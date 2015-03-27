package com.hatarakuma.yomite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class Start extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
		
		Button btnStart1 = (Button)findViewById(R.id.btnStart1);
		Button btnStart2 = (Button)findViewById(R.id.btnStart2);
        
        btnStart1.setOnClickListener(this);
        btnStart2.setOnClickListener(this);
        
        /*Spinner rotationSpinner = (Spinner)findViewById(R.id.spnRotationIndex);
        rotationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				SetRotationAmount(arg0.getSelectedItem().toString());
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
        Cipher indexCipher = new Cipher(12);
        EditText here = (EditText)findViewById(R.id.editText1);
        here.setText(indexCipher.Encrypt("Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived and so dedicated, can long endure. We are met on a great battle-field of that war."));*/
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btnStart1) {
			//Intent i = new Intent("com.hatarakuma.yomite.FrameMarkers");
			//startActivity(i);
			((IndexVar) this.getApplication()).setIndex(12);
			Intent intent = new Intent(Start.this, FrameMarkers.class);
			startActivity(intent);
		} else {
			((IndexVar) this.getApplication()).setIndex(11);
			Intent intent = new Intent(Start.this, FrameMarkers.class);
			startActivity(intent);
		}
	} 
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // exit app
        	finish();
        }
        
        return super.onKeyDown(keyCode, event);
    }
}
