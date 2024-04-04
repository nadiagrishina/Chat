package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
;import android.text.format.DateFormat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static int sign_code = 1;
    private RelativeLayout activity_main;
    private FirebaseListAdapter<Message> adapter;
    private FloatingActionButton sendButton;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == sign_code){
            if(resultCode == RESULT_OK){
                Snackbar.make(activity_main, "Вы авторизованы", Snackbar.LENGTH_LONG).show();
                displayAllMessages();
            }
            else {
                Snackbar.make(activity_main, "Вы не авторизованы", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main = findViewById(R.id.activity_main);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textField = findViewById(R.id.messageField);
                if(textField.getText().toString() == ""){
                    return;
                }
                FirebaseDatabase.getInstance("https://chat-f54a8-default-rtdb.firebaseio.com/").getReference().push().setValue(
                        new Message(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                textField.getText().toString()
                        )
                );
                textField.setText("");

            }
        });
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), sign_code);
        else {
            Snackbar.make(activity_main, "Вы авторизованы", Snackbar.LENGTH_LONG).show();
            displayAllMessages();
        }
    }

    private void displayAllMessages() {
        ListView messages = (ListView) findViewById(R.id.list_of_messages);
        FirebaseListOptions.Builder<Message> options = new FirebaseListOptions.Builder<Message>();
        options
                .setQuery(FirebaseDatabase.getInstance("https://chat-f54a8-default-rtdb.firebaseio.com/").getReference(), Message.class)
                .setLayout(R.layout.list_item).setLifecycleOwner(this);
        adapter = new FirebaseListAdapter<Message>(options.build()) {
            @Override
            protected void populateView(View v, Message model, int position) {

                TextView messUser, messTime;
                BubbleTextView messText;
                RelativeLayout messageView = (RelativeLayout) v.findViewById(R.id.listItemWrapper);
                messUser = messageView.findViewById(R.id.messageUser);
                messTime = messageView.findViewById(R.id.messageTime);
                messText = messageView.findViewById(R.id.messageText);

                messUser.setText(model.getUserName());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    messTime.setText(DateFormat.format("dd-MM-yyyy HH:mm:ss", model.getMessageTime()));
                }
                messText.setText(model.getTextMessage());
            }
        };
        messages.setAdapter(adapter);
    }

}