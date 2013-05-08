package pl.edu.agh.io.coordinator;

import java.util.Set;

import pl.edu.agh.io.coordinator.LayersMenuFragment.OnFragmentInteractionListener;
import pl.edu.agh.io.coordinator.resources.User;
import pl.edu.agh.io.coordinator.utils.net.IJSonProxy;
import pl.edu.agh.io.coordinator.utils.net.JSonProxy;
import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidSessionIDException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.NetworkException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainMapActivity extends Activity implements
		OnFragmentInteractionListener {

	private TextView debugInfo;
	private boolean loggingOut = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main_map);

		debugInfo = (TextView) findViewById(R.id.debugInfo);
		debugInfo.setText("Debug");

		/*new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						new GetUsersInBackground().execute(new Intent());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();*/

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actionChat:
			item.setChecked(!item.isChecked());
			debugInfo.append("\n" + item.getTitle() + "()");
			return true;
		case R.id.actionLayers:
			debugInfo.append("\n" + item.getTitle() + "()");
			new GetUsersInBackground().execute(new Intent());
			return true;
		case R.id.actionCreateGroup:
			Intent intentCreateGroup = new Intent(MainMapActivity.this,
					CreateGroupActivity.class);
			startActivity(intentCreateGroup);
			return true;
		case R.id.actionRemoveGroup:
			Intent intentRemoveGroup = new Intent(MainMapActivity.this,
					RemoveGroupActivity.class);
			startActivity(intentRemoveGroup);
			return true;
		case R.id.actionCreateItem:
			Intent intentCreateItem = new Intent(MainMapActivity.this,
					CreateUserItemActivity.class);
			startActivity(intentCreateItem);
			return true;
		case R.id.actionSettings:
			Intent intentSettings = new Intent(MainMapActivity.this,
					NotImplementedYetActivity.class);
			startActivity(intentSettings);
			return true;
		case R.id.actionLogout:
			if (!loggingOut) {
				loggingOut = true;
				debugInfo.append("\n" + item.getTitle() + "()");
				Intent intent = new Intent();
				new LogoutInBackground().execute(intent);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// alerts about invalid sessionID and finishes activity
	private void invalidSessionId() {
		new AlertDialog.Builder(MainMapActivity.this)
				.setMessage(R.string.alert_invalid_session_id_logout)
				.setTitle(R.string.alert_invalid_session_id)
				.setCancelable(false)
				.setIcon(R.drawable.alerts_and_states_warning)
				.setPositiveButton(R.string.button_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								MainMapActivity.this.finish();
							}
						}).create().show();
	}

	// shows network problem alert
	private void networkProblem() {
		Toast.makeText(getApplicationContext(), R.string.alert_network_problem,
				Toast.LENGTH_LONG).show();
	}

	private class LogoutInBackground extends AsyncTask<Intent, Void, Exception> {

		@Override
		protected Exception doInBackground(Intent... params) {
			IJSonProxy proxy = JSonProxy.getInstance();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				proxy.logout();
			} catch (InvalidSessionIDException e) {
				return e;
			} catch (NetworkException e) {
				return e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Exception result) {

			if (result == null) {
				MainMapActivity.this.finish();
			} else if (result instanceof NetworkException) {
				networkProblem();
				loggingOut = false;
			} else if (result instanceof InvalidSessionIDException) {
				invalidSessionId();
			}
		}

	}

	private class GetUsersInBackground extends
			AsyncTask<Intent, Void, Exception> {

		private Set<User> users;

		@Override
		protected Exception doInBackground(Intent... params) {
			IJSonProxy proxy = JSonProxy.getInstance();

			try {
				users = proxy.getUsers();
			} catch (InvalidSessionIDException e) {
				return e;
			} catch (NetworkException e) {
				return e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Exception result) {

			if (result == null) {
				for (User user : users)
					debugInfo.append("\n" + user.getName() + " "
							+ user.getSurname());
			} else if (result instanceof NetworkException) {
				networkProblem();
			} else if (result instanceof InvalidSessionIDException) {
				invalidSessionId();
			}
		}

	}

	@Override
	public void onFragmentInteraction(Uri uri) {
		// TODO Auto-generated method stub

	}

}
