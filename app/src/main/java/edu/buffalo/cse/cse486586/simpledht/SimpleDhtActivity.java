package edu.buffalo.cse.cse486586.simpledht;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class SimpleDhtActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_dht_main);
        
        final TextView tv = (TextView) findViewById(R.id.textView1);
        final TextView remTv = (TextView) findViewById(R.id.remote_text_display);
        tv.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.button3).setOnClickListener(
                new OnTestClickListener(tv, getContentResolver()));

        final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
        findViewById(R.id.button2).setOnClickListener(
                new View.OnClickListener() {

                    @Override

                    public void onClick(View v) {

                        Cursor resultCursor = getContentResolver().query(mUri, null, "aNC7OIviGlY7hvtBbigg1WvoWoaXdLMw", null, null);
                        if (resultCursor.moveToFirst()){
                            do{
                                remTv.append(resultCursor.getString(resultCursor.getColumnIndex("value")));
                                remTv.append("\n");
                                // do what ever you want here
                            }while(resultCursor.moveToNext());
                        }
                        resultCursor.close();

                    }

                });
        findViewById(R.id.button1).setOnClickListener(
                new View.OnClickListener() {

                    @Override

                    public void onClick(View v) {

                        Cursor resultCursor = getContentResolver().query(mUri, null, "aNC7OIviGlY7hvtBbigg1WvoWoaXdLMw", null, null);
                        if (resultCursor.moveToFirst()){
                            do{
                                remTv.append(resultCursor.getString(resultCursor.getColumnIndex("value")));
                                remTv.append("\n");
                            }while(resultCursor.moveToNext());
                        }
                        resultCursor.close();

                    }

                });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_simple_dht_main, menu);
        return true;
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
}
