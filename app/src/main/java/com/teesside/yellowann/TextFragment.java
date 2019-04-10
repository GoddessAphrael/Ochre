package com.teesside.yellowann;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import static android.content.Context.TEXT_SERVICES_MANAGER_SERVICE;

public class TextFragment extends Fragment implements SpellCheckerSession.SpellCheckerSessionListener
{
    private String currentText, currentTextPath;
    private TextView convertedText, suggestion1, suggestion2, suggestion3;
    private EditText textEdit;
    private ProgressBar progressBar;
    private AppCompatImageButton editText, analyzeText;
    private CheckBox favouriteStar;
    private Button spellPass, spellCancel, editAccept, editCancel;
    private List<String> spellCheck = new ArrayList<>();
    private int counter, size;
    private TextServicesManager tsm;
    private SpellCheckerSession session;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private StorageMetadata metaData;
    private UploadTask uploadTask;
    private DatabaseReference mDataRef, downloadRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.text);
        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState)
    {
        super.onViewCreated(v, savedInstanceState);

        convertedText = v.findViewById(R.id.new_text);
        suggestion1 = v.findViewById(R.id.spellcheck_suggestion_1);
        suggestion2 = v.findViewById(R.id.spellcheck_suggestion_2);
        suggestion3 = v.findViewById(R.id.spellcheck_suggestion_3);
        textEdit = v.findViewById(R.id.edit_new_text);
        progressBar = v.findViewById(R.id.uploadProgress_text);
        editText = v.findViewById(R.id.edit_text);
        analyzeText = v.findViewById(R.id.analyze_text);
        favouriteStar = v.findViewById(R.id.favourite_star_text);
        spellPass = v.findViewById(R.id.spellcheck_pass);
        spellCancel = v.findViewById(R.id.spellcheck_cancel);
        editAccept = v.findViewById(R.id.edit_accept);
        editCancel = v.findViewById(R.id.edit_cancel);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mDataRef = FirebaseDatabase.getInstance().getReference();
        downloadRef = FirebaseDatabase.getInstance().getReference().child("user")
                .child(mAuth.getCurrentUser().getUid()).child("textDownload");

        Bundle arguments = getArguments();

        String test = "anohter test snetence? this tmie whith, punctaution, too!";
        convertedText.setText(test);

        if (arguments != null)
        {
            currentText = arguments.getString("text");
            convertedText.setText(currentText);
            documentsAddText();
        }

        favouriteStar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.w("FavouriteStar", "FavouriteStar:Pressed");
                Toast.makeText(getActivity(), "Unable to Favourite: Not Implemented",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // initiate popup-menu
        editText.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v)
            {
                final String TAG = "editText";
                PopupMenu popup = new PopupMenu(getActivity(), editText);
                popup.getMenuInflater().inflate(R.menu.popup_text, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        switch(menuItem.getItemId())
                        {
                            case R.id.text_edit:
                                currentText = convertedText.getText().toString();
                                textEdit.setText(convertedText.getText());
                                convertedText.setVisibility(View.INVISIBLE);
                                textEdit.setVisibility(View.VISIBLE);
                                editAccept.setVisibility(View.VISIBLE);
                                editCancel.setVisibility(View.VISIBLE);
                                break;
                            case R.id.text_spellcheck:
                                spellPass.setVisibility(View.VISIBLE);
                                spellCancel.setVisibility(View.VISIBLE);
                                final String str[] = convertedText.getText().toString().split(" ");
                                spellCheck = Arrays.asList(str);
                                size = spellCheck.size();
                                counter = 0;
                                checkSpell(spellCheck, size);
                                break;
                            case R.id.text_open_local:
                                loadLocalTextConfirm();
                                break;
                            case R.id.text_open_cloud:
                                Log.w(TAG, "text_open_cloud:Pressed");
                                Toast.makeText(getActivity(), "Unable to Open: Not Implemented",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.text_save:
                                uploadToCloud();
                                break;
                            case R.id.text_delete:
                                Log.w(TAG, "text_delete:Pressed");
                                Toast.makeText(getActivity(), "Unable to Delete: Not Implemented",
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return true;
                    }
                });
                MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popup.getMenu(), editText);
                menuHelper.setForceShowIcon(true);
                menuHelper.show();
            }
        });

        // save current text state and set open editText
        editAccept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                convertedText.setText(textEdit.getText());
                convertedText.setVisibility(View.VISIBLE);
                textEdit.setVisibility(View.INVISIBLE);
                editAccept.setVisibility(View.INVISIBLE);
                editCancel.setVisibility(View.INVISIBLE);
                documentsAddText();
            }
        });

        // return text to previous text state
        editCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                convertedText.setText(currentText);
                textEdit.setVisibility(View.INVISIBLE);
                convertedText.setVisibility(View.VISIBLE);
            }
        });

        // initialise analysisFragment
        analyzeText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new AnalysisFragment()).commit();
                    }
                });
            }
        });

        // skip current spellcheck options
        spellPass.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                check();
            }
        });

        // cancel current spellcheck
        spellCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                session.cancel();
                suggestion1.setText(null);
                suggestion2.setText(null);
                suggestion3.setText(null);
                spellPass.setVisibility(View.INVISIBLE);
                spellCancel.setVisibility(View.INVISIBLE);
                Log.d("spellCheck", "spellCheck:Cancel");
                Toast.makeText(getActivity(), "SpellCheck Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // set text1 for current spellcheck suggestion
        suggestion1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (suggestion1 != null)
                {
                    spellCheck.set(counter, suggestion1.getText().toString());
                    check();
                }
            }
        });

        // set text2 for current spellcheck suggestion
        suggestion2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (suggestion2 != null)
                {
                    spellCheck.set(counter, suggestion2.getText().toString());
                    check();
                }
            }
        });

        // set text3 for current spellcheck suggestion
        suggestion3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (suggestion3 != null)
                {
                    spellCheck.set(counter, suggestion3.getText().toString());
                    check();
                }
            }
        });
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] results)
    {

    }

    // spellcheck suggestions for current text
    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results)
    {
        final ArrayList<String> suggestions = new ArrayList<>();
        for(SentenceSuggestionsInfo result:results)
        {
            int n = result.getSuggestionsCount();
            for(int i=0; i < n; i++)
            {
                int m = result.getSuggestionsInfoAt(i).getSuggestionsCount();

                if((result.getSuggestionsInfoAt(i).getSuggestionsAttributes() &
                    SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO) != SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO )
                continue;

                for (int k = 0; k < m; k++)
                {
                    suggestions.add(result.getSuggestionsInfoAt(i).getSuggestionAt(k));
                }
            }
        }

        if (suggestions.size() > 0)
        {
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    switch (suggestions.size())
                    {
                        case 1:
                            suggestion1.setText(null);
                            suggestion2.setText(suggestions.get(0));
                            suggestion3.setText(null);
                            break;
                        case 2:
                            suggestion1.setText(suggestions.get(0));
                            suggestion2.setText(null);
                            suggestion3.setText(suggestions.get(1));
                            break;
                        case 3:
                            suggestion1.setText(suggestions.get(0));
                            suggestion2.setText(suggestions.get(1));
                            suggestion3.setText(suggestions.get(2));
                            break;
                    }
                }
            });
        }
        else
        {
            counter += 1;
            checkSpell(spellCheck, size);
        }
    }

    // initiate new spellcheck session
    private void fetchSuggestionsFor(String input)
    {
        tsm = (TextServicesManager) getActivity().getSystemService(TEXT_SERVICES_MANAGER_SERVICE);

        session = tsm.newSpellCheckerSession(null, Locale.ENGLISH, this,true);

        session.getSentenceSuggestions(
                new TextInfo[]{ new TextInfo(input) },
                3
        );
    }

    // if more words in current text, fetch next suggestion, else save text and display
    private void checkSpell(List<String> spellCheck, int size)
    {
        if (counter < size)
        {
            fetchSuggestionsFor(spellCheck.get(counter));
        }
        else
        {
            suggestion1.setText(null);
            suggestion2.setText(null);
            suggestion3.setText(null);
            spellPass.setVisibility(View.INVISIBLE);
            spellCancel.setVisibility(View.INVISIBLE);
            Log.d("spellCheck", "spellCheck:Complete");
            Toast.makeText(getActivity(), "SpellCheck Complete", Toast.LENGTH_SHORT).show();
            documentsAddText();
        }
    }

    // show suggestions for current word
    private void check()
    {
        StringBuilder newText = new StringBuilder();
        for (String t: spellCheck)
        {
            newText.append(t);
            newText.append(" ");
        }
        convertedText.setText(newText);
        counter += 1;
        checkSpell(spellCheck, size);
    }

    // create directory if need, else save text to local
    private void documentsAddText()
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
        String textFileName = "TEXT_" + timeStamp + "_";
        File directory = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!directory.exists())
        {
            directory.mkdir();
        }
        File text = new File(directory + "/" + textFileName + ".txt");

        currentTextPath = text.getAbsolutePath();

        try
        {
            FileOutputStream outputStream = new FileOutputStream(text);
            outputStream.write(convertedText.getText().toString().getBytes());
            outputStream.close();
        }
        catch (IOException e)
        {
            Log.d("documentsAddText", "documentsAddText:Failure");
            Toast.makeText(getActivity(), "Error Saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // save text to cloud
    private void uploadToCloud()
    {
        final File f = new File(currentTextPath);
        final Uri uri = Uri.fromFile(f);

        mDataRef.child("user/" + mAuth.getCurrentUser().getUid() + "/textDownload/"
                + FilenameUtils.getBaseName(uri.getLastPathSegment())).setValue(convertedText.getText().toString());

    }

    // dialog confirm local load
    private void loadLocalTextConfirm()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true).setTitle("Load").setMessage("Load text?");

        builder.setPositiveButton(R.string.open_local, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ((MainActivity) getActivity()).loadLocalText();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
