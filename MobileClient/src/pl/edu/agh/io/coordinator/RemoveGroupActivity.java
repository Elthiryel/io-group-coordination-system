package pl.edu.agh.io.coordinator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class RemoveGroupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remove_group);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.remove_group, menu);
		return true;
	}

}
