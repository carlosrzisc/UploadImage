package com.example.uploadimage.uploadimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

// https://www.thecrazyprogrammer.com/2016/09/android-upload-image-server-using-volley.html
public class MainActivity extends AppCompatActivity {
    ImageView image;
    Button choose, upload;
    EditText endpoint;
    int PICK_IMAGE_REQUEST = 111;
    //String URL ="http://192.168.1.101/JavaRESTfullWS/DemoService/upload";
    Bitmap bitmap;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView)findViewById(R.id.image);
        choose = (Button)findViewById(R.id.choose);
        upload = (Button)findViewById(R.id.upload);
        endpoint = (EditText)findViewById(R.id.endpoint);
        progress = (ProgressBar)findViewById(R.id.progressbar);

        //opening image chooser option
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap == null) {
                    Toast.makeText(MainActivity.this, "Please choose an image", Toast.LENGTH_LONG).show();
                    return;
                }
                String URL = endpoint.getText().toString();
                if (URL.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a URL endpoint", Toast.LENGTH_LONG).show();
                    return;
                }

                progress.setVisibility(ProgressBar.VISIBLE);

                //converting image to base64 string
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                //sending image to server
                StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progress.setVisibility(ProgressBar.INVISIBLE);
                        if (s.equals("true")) {
                            Toast.makeText(MainActivity.this, "Uploaded Successful", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Some error occurred!", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progress.setVisibility(ProgressBar.INVISIBLE);
                        Toast.makeText(MainActivity.this, "Some error occurred -> " + volleyError, Toast.LENGTH_LONG).show();
                    }
                }) {
                    //adding parameters to send
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> parameters = new HashMap<>();
                        parameters.put("image", imageString);
                        return parameters;
                    }
                };

                RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
                rQueue.add(request);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();

            try {
                //getting image from gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                //Setting image to ImageView
                image.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
