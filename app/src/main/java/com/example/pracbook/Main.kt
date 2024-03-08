package com.example.pracbook

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory
import com.amazonaws.regions.Regions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.squareup.picasso.Picasso
import kotlin.random.Random


fun getRecIndex(): Int {
    // Function body
    // Gets a random integer from 0-9999
    return Random.nextInt(0, 10000)
}


class Main : AppCompatActivity() {

    val functionName = "BookRec"

    // Initilizes Firebase authentication extension to auth variable
    val auth = FirebaseAuth.getInstance()
    // Initilizes Firebase realtime database extension to database variable
    var database = Firebase.database.reference

    // When activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "onCreate() in MainActivity is called");
        // Shows the main activity page
        setContentView(R.layout.activity_main)

        // Gets all elements in main activity page
        val button = findViewById<Button>(R.id.logout)
        val textView = findViewById<TextView>(R.id.user_details)
        val user = auth.getCurrentUser();

        val getBookButton = findViewById<Button>(R.id.btn_getbook)
        val bookTextView = findViewById<TextView>(R.id.book)
        val bookCoverImage = findViewById<ImageView>(R.id.book_cover)

        val likeBookButton = findViewById<Button>(R.id.btn_likebook)
        val dislikeBookButton = findViewById<Button>(R.id.btn_dislikebook)

        val lambdaButton = findViewById<Button>(R.id.btn_Lambda)

        // Bottom of screen to display the user's email
        // Check if user is null, if null takes user back to login page
        // Else sets text view to user's email
        if(user == null){
            val intent = Intent(getApplicationContext(), Login::class.java)
            startActivity(intent)
            finish()
        }else{
            textView.setText(user.getEmail())
        }

        // When "LOGOUT" button is clicked, takes user back to login activity page
        button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(getApplicationContext(), Login::class.java)
            startActivity(intent)
            finish()
        }

        // If "GET BOOK" button is clicked
        getBookButton.setOnClickListener {



            // Gets data inside index
            database.child("Books").child("0").child(getRecIndex().toString()).get().addOnSuccessListener {

                // Sets text view to title of book
                bookTextView.setText(it.child("actualTitle").value.toString())

                // Gets book URL and loads it into image view
                val url = it.child("img").value.toString()
                Picasso.with(this).load(url).into(bookCoverImage)

            }.addOnFailureListener {
                Toast.makeText(
                    baseContext,
                    "Error getting data",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        // If "LIKE" button is clicked
        likeBookButton.setOnClickListener {

            // Gets the data of the user in UserPreferences based on user's UID
            // If new user, creates a new element in data base
            val data = database.child("UserPreferences").child(auth.currentUser?.uid.toString()).child("Liked")

            // Pushes liked book's title into "Liked" subsection in database
            data.push().setValue(bookTextView.getText().toString()).addOnSuccessListener {
                Toast.makeText(
                    baseContext,
                    "Added book to liked",
                    Toast.LENGTH_SHORT,
                ).show()
            }.addOnFailureListener{
                Toast.makeText(
                    baseContext,
                    "Unable to add book to liked",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        // If "DISLIKE" button is clicked
        dislikeBookButton.setOnClickListener {

            // Gets the data of the user in UserPreferences based on user's UID
            // If new user, creates a new element in data base
            val data = database.child("UserPreferences").child(auth.currentUser?.uid.toString()).child("Disliked")

            // Pushes disliked book's title into "Disliked" subsection in database
            data.push().setValue(bookTextView.getText().toString()).addOnSuccessListener {
                Toast.makeText(
                    baseContext,
                    "Added book to disliked",
                    Toast.LENGTH_SHORT,
                ).show()
            }.addOnFailureListener{
                Toast.makeText(
                    baseContext,
                    "Unable to add book to disliked",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        lambdaButton.setOnClickListener {

            val cognitoProvider = CognitoCachingCredentialsProvider(
                this.applicationContext,
                "us-east-1:4ad44a79-d577-4f0d-863c-7cb76f255372",
                Regions.US_WEST_1
            )

            val factory = LambdaInvokerFactory(
                this.applicationContext,
                Regions.US_WEST_1, cognitoProvider
            )

            val myInterface = factory.build(MyInterface::class.java)

            val request = RequestClass("John", "Doe")

            object : AsyncTask<RequestClass, Void, ResponseClass>() {
                override fun doInBackground(vararg params: RequestClass): ResponseClass? {
                    // invoke "echo" method. In case it fails, it will throw a
                    // LambdaFunctionException.
                    return try {
                        myInterface.BookRec(params[0])
                    } catch (lfe: LambdaFunctionException) {
                        Log.e("Tag", "Failed to invoke echo", lfe)
                        null
                    }
                }

                override fun onPostExecute(result: ResponseClass?) {
                    if (result == null) {
                        return
                    }

                    // Do a toast
                    Toast.makeText(this@Main, result.getGreetings(), Toast.LENGTH_SHORT).show()
                }
            }.execute(request)
        }
    }
    // When activity is resumed
    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "onResume() in MainActivity is called")
    }

    // When activity is paused
    override fun onPause() {
        super.onPause()
        Log.d("Lifecycle", "onPause() in MainActivity is called")
    }

    // When activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        Log.d("Lifecycle", "onDestroy() in MainActivity is called")
    }
}