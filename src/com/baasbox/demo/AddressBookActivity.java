package com.baasbox.demo;

import java.util.ArrayList;
import java.util.List;

import com.baasbox.android.BaasClientException;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasException;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasServerException;
import com.baasbox.demo.util.AlertUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class AddressBookActivity extends ListActivity implements
		ActionMode.Callback {

	private static final int MENUITEM_REFRESH = 1;
	private static final int MENUITEM_ADD = 2;
	private static final int MENUITEM_DELETE = 3;

	private ListTask listTask;
	private AddTask addTask;
	private ArrayAdapter<BaasDocument> adapter;
	private MenuItem refreshMenuItem;
	private int selectedItem = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.adapter = new Adapter(this);
		this.setListAdapter(adapter);

		this.getListView().setLongClickable(true);
		this.getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		this.getListView().setOnItemLongClickListener(
				new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> adapter,
							View view, int position, long id) {
						if (selectedItem != -1) {
							return false;
						}

						selectedItem = position;
						startActionMode(AddressBookActivity.this);
						view.setSelected(true);
						return true;
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		refreshMenuItem = menu.add(Menu.NONE, MENUITEM_REFRESH, Menu.NONE,
				"Refresh");
		refreshMenuItem.setIcon(R.drawable.ic_menu_refresh);
		refreshMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		if (listTask != null && listTask.getStatus() == Status.RUNNING)
			refreshMenuItem.setActionView(R.layout.view_menuitem_refresh);

		MenuItem add = menu.add(Menu.NONE, MENUITEM_ADD, Menu.NONE, "Add");
		add.setIcon(R.drawable.ic_menu_add);
		add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case MENUITEM_ADD:
			onClickAddPerson();
			break;
		case MENUITEM_REFRESH:
			refresh();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case MENUITEM_DELETE:
			delete(selectedItem);
			mode.finish();
			break;
		default:
			return super.onContextItemSelected(item);
		}

		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		BaasDocument person = adapter.getItem(selectedItem);
		mode.setTitle(person.getString("name"));

		MenuItem delete = menu.add(ContextMenu.NONE, MENUITEM_DELETE,
				ContextMenu.NONE, "Delete");
		delete.setIcon(R.drawable.ic_menu_delete);
		delete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		selectedItem = -1;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	protected void delete(int position) {
		BaasDocument person = adapter.getItem(position);
		adapter.remove(person);
		new DeleteTask().execute(person);
	}

	private void refresh() {
		listTask = new ListTask();
		listTask.execute();
	}

	private void onClickAddPerson() {
		View layout = getLayoutInflater().inflate(R.layout.dialog_add, null);
		final EditText nameText = (EditText) layout.findViewById(R.id.name);
		final EditText phoneText = (EditText) layout.findViewById(R.id.phone);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setNegativeButton("Cancel", null);
		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = nameText.getText().toString().trim();
				String phone = phoneText.getText().toString().trim();

				if (name.length() > 0 && phone.length() > 0)
					addPerson(name, phone);
			}
		});

		builder.create().show();
	}

	protected void addPerson(String name, String phone) {
		addTask = new AddTask();
		addTask.execute(name, phone);
	}

	public void onPersonAdded(BaasResult<BaasDocument> result) {
		try {
			adapter.add(result.get());
			adapter.notifyDataSetChanged();
		} catch (BaasClientException e) {
			AlertUtils.showErrorAlert(this, e);
		} catch (BaasServerException e) {
			AlertUtils.showErrorAlert(this, e);
		} catch (BaasException e) {
			AlertUtils.showErrorAlert(this, e);
		}
	}

	protected void onListReceived(BaasResult<List<BaasDocument>> result) {
		try {
			List<BaasDocument> array = result.get();
			adapter.clear();

			for (int i = 0; i < array.size(); i++)
				adapter.add(array.get(i));

			adapter.notifyDataSetChanged();
		} catch (BaasClientException e) {
			AlertUtils.showErrorAlert(this, e);
		} catch (BaasServerException e) {
			AlertUtils.showErrorAlert(this, e);
		} catch (BaasException e) {
			AlertUtils.showErrorAlert(this, e);
		}
	}
	
	protected void onPersonDeleted(BaasResult<Void> result) {
		try {
			result.get();
		} catch (BaasClientException e) {
			AlertUtils.showErrorAlert(this, e);
		} catch (BaasServerException e) {
			AlertUtils.showErrorAlert(this, e);
		} catch (BaasException e) {
			AlertUtils.showErrorAlert(this, e);
		}
	}

	public class ListTask extends AsyncTask<Void, Void, BaasResult<List<BaasDocument>>> {

		@Override
		protected void onPreExecute() {
			if (refreshMenuItem != null)
				refreshMenuItem.setActionView(R.layout.view_menuitem_refresh);
		}

		@Override
		protected BaasResult<List<BaasDocument>> doInBackground(Void... params) {
			return BaasDocument.fetchAllSync("address-book");
		}

		@Override
		protected void onPostExecute(BaasResult<List<BaasDocument>> result) {
			if (refreshMenuItem != null)
				refreshMenuItem.setActionView(null);
			onListReceived(result);
		}
	}

	public class AddTask extends AsyncTask<String, Void, BaasResult<BaasDocument>> {

		@Override
		protected BaasResult<BaasDocument> doInBackground(String... params) {
			BaasDocument person = new BaasDocument("address-book");

			person.putString("name", params[0]);
			person.putString("phone", params[1]);
			

			return person.saveSync();
		}

		@Override
		protected void onPostExecute(BaasResult<BaasDocument> result) {
			onPersonAdded(result);
		}
	}

	public class DeleteTask extends	AsyncTask<BaasDocument, Void, BaasResult<Void>> {
		
		@Override
		protected BaasResult<Void> doInBackground(BaasDocument... params) {
			return params[0].deleteSync();
		}
		
		@Override
		protected void onPostExecute(BaasResult<Void> result) {
			onPersonDeleted(result);
		}
	}

	public class Adapter extends ArrayAdapter<BaasDocument> {

		public Adapter(Context context) {
			super(context, android.R.layout.simple_list_item_2,	new ArrayList<BaasDocument>());
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(android.R.layout.simple_list_item_2,
						null);

				Tag tag = new Tag();
				tag.text1 = (TextView) view.findViewById(android.R.id.text1);
				tag.text2 = (TextView) view.findViewById(android.R.id.text2);
				view.setTag(tag);
			}

			Tag tag = (Tag) view.getTag();
			BaasDocument entry = getItem(position);
			tag.text1.setText(entry.getString("name"));
			//tag.text2.setText(entry.optString("phone"));

			return view;
		}

	}

	protected static class Tag {

		public TextView text1;
		public TextView text2;

	}

}
