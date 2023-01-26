package com.example.trackforsafe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class RegisterActivity extends AppCompatActivity {

    TextInputLayout name, phone, email, password, repassword;
    TextView invalidpass, invalidemail, invalidphone, haveaccount;
    Button registerbtn;
    ProgressBar progressBar2;
    Uri imgUri;
    CircleImageView profile;
    FloatingActionButton changeprofile;
    FirebaseAuth mAuth;
    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://track-for-safe.appspot.com");
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://track-for-safe-default-rtdb.firebaseio.com/");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[@#$%^&+=])" +     // at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{8,}" +                // at least 8 characters
                    "$");

//    public static final String MSG = "com.example.loginregister.INFO";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        profile = findViewById(R.id.profile_image);
        changeprofile = findViewById(R.id.uploadbtn);
        name = findViewById(R.id.nameLayout);
        phone = findViewById(R.id.phonenumberLayout);
        invalidphone = findViewById(R.id.invalidephone);
        email = findViewById(R.id.emailforforgotLayout);
        invalidemail = findViewById(R.id.invalidemail);
        password = findViewById(R.id.passwordLayout);
        invalidpass = findViewById(R.id.invalidpass);
        repassword = findViewById(R.id.confpasswordLayout);
        registerbtn = findViewById(R.id.registerbutton);
        haveaccount = findViewById(R.id.login);
        progressBar2 = findViewById(R.id.progressBar2);
        mAuth = FirebaseAuth.getInstance();

        ActivityResultLauncher<Intent> launcher=
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                    if(result.getResultCode()==RESULT_OK) {
                        imgUri = Objects.requireNonNull(result.getData()).getData();
                        profile.setImageURI(imgUri);
                    }
                });
        changeprofile.setOnClickListener(view -> ImagePicker.Companion.with(this)
                .crop()
                .cropOval()
                .maxResultSize(512,512,true)
                .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                .createIntentFromDialog(new Function1(){
                    public Object invoke(Object var1){
                        this.invoke((Intent)var1);
                        return Unit.INSTANCE;
                    }

                    public void invoke(@NotNull Intent it){
                        Intrinsics.checkNotNullParameter(it,"it");
                        launcher.launch(it);
                    }
                }));

        registerbtn.setOnClickListener(view -> {
            final String name1 = Objects.requireNonNull(name.getEditText()).getText().toString().trim();
            final String phone1 = Objects.requireNonNull(phone.getEditText()).getText().toString().trim();
            final String email1 = Objects.requireNonNull(email.getEditText()).getText().toString().trim();
            final String password1 = Objects.requireNonNull(password.getEditText()).getText().toString().trim();
            final String repassword1 = Objects.requireNonNull(repassword.getEditText()).getText().toString().trim();

            if(name1.equals("") || phone1.equals("") || email1.equals("") || password1.equals("") || repassword1.equals("")){
                Toast.makeText(this, "Please enter required fields", Toast.LENGTH_SHORT).show();
            }
            else if (!password1.equals(repassword1)) {
                    Toast.makeText(this, "Password are not matching", Toast.LENGTH_SHORT).show();
            }
            else {
                if(!validatePassword() | !validatePhone() | !validateEmail()){
                    return;
                }
                else {

                    StorageReference imageRef = storageReference.child(imgUri.getLastPathSegment());
                    UploadTask uploadTask = imageRef.putFile(imgUri);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, "Image Upload Error : " + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
                            databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.hasChild(phone1)) {
                                        Toast.makeText(RegisterActivity.this, "Phone Number is already registered", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        registerbtn.setVisibility(View.GONE);
                                        progressBar2.setVisibility(View.VISIBLE);

                                        databaseReference.child("Users").child(phone1).child("Name").setValue(name1);
                                        databaseReference.child("Users").child(phone1).child("Phone Number").setValue(phone1);
                                        databaseReference.child("Users").child(phone1).child("Password").setValue(password1);
                                        finish();
                                        mAuth.createUserWithEmailAndPassword(email1, password1)
                                                .addOnCompleteListener(RegisterActivity.this, task -> {
                                                    if (task.isSuccessful()) {
                                                        registerbtn.setVisibility(View.VISIBLE);
                                                        progressBar2.setVisibility(View.GONE);
                                                        Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                    } else {
                                                        registerbtn.setVisibility(View.VISIBLE);
                                                        progressBar2.setVisibility(View.GONE);
                                                        Toast.makeText(RegisterActivity.this, "Registered Error : " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    }
                }
        });

        haveaccount.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        });
    }


    @SuppressLint("SetTextI18n")
    private boolean validatePassword() {
        String password1 = Objects.requireNonNull(password.getEditText()).getText().toString().trim();
        if(!PASSWORD_PATTERN.matcher(password1).matches()){
            invalidpass.setText("Password is too weak");
            return false;
        }
        else{
            invalidpass.setText("");
            return true;
        }
    }


    @SuppressLint("SetTextI18n")
    private boolean validateEmail() {
        String email1 = Objects.requireNonNull(email.getEditText()).getText().toString().trim();
        if(!Patterns.EMAIL_ADDRESS.matcher(email1).matches()){
            invalidemail.setText("Please enter valid email address");
            return false;
        }
        else{
            invalidemail.setText("");
            return true;
        }
    }
    @SuppressLint("SetTextI18n")
    private boolean validatePhone() {
        String phone1 = Objects.requireNonNull(phone.getEditText()).getText().toString().trim();
        if(phone1.length() < 10){
            invalidphone.setText("Please enter valid phone number");
            return false;
        }
        else{
            invalidphone.setText("");
            return true;
        }
    }
}