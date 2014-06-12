/*
  This file is part of Subsonic.
	Subsonic is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	Subsonic is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.
	You should have received a copy of the GNU General Public License
	along with Subsonic. If not, see <http://www.gnu.org/licenses/>.
	Copyright 2014 (C) Scott Jackson
*/

package github.daneren2005.dsub.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import github.daneren2005.dsub.R;
import github.daneren2005.dsub.domain.User;
import github.daneren2005.dsub.fragments.SubsonicFragment;
import github.daneren2005.dsub.service.MusicService;
import github.daneren2005.dsub.service.MusicServiceFactory;
import github.daneren2005.dsub.service.OfflineException;
import github.daneren2005.dsub.service.ServerTooOldException;
import github.daneren2005.dsub.view.SettingsAdapter;

public final class UserUtil {
	private static User currentUser;

	public static void seedCurrentUser(final Context context) {
		// Only try to seed if online
		if(Util.isOffline(context)) {
			return;
		}

		new SilentBackgroundTask<Void>(context) {
			@Override
			protected Void doInBackground() throws Throwable {
				currentUser = MusicServiceFactory.getMusicService(context).getUser(false, getCurrentUsername(context), context, null);
				return null;
			}
		}.execute();
	}

	public static User getCurrentUser(Context context) {
		return currentUser;
	}

	public static String getCurrentUsername(Context context, int instance) {
		SharedPreferences prefs = Util.getPreferences(context);
		return prefs.getString(Constants.PREFERENCES_KEY_USERNAME + instance, null);
	}

	public static String getCurrentUsername(Context context) {
		return getCurrentUsername(context, Util.getActiveServer(context));
	}

	public static boolean isCurrentAdmin() {
		if(currentUser == null) {
			return false;
		} else {
			return isCurrentRole("adminRole");
		}
	}

	public static boolean isCurrentRole(String role) {
		if(currentUser == null) {
			return false;
		}

		for(User.Setting setting: currentUser.getSettings()) {
			if(setting.getName().equals(role)) {
				return setting.getValue() == true;
			}
		}

		return false;
	}

	public static void changePassword(final Activity context, final User user) {
		View layout = context.getLayoutInflater().inflate(R.layout.change_password, null);
		final TextView passwordView = (TextView) layout.findViewById(R.id.new_password);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.admin_change_password)
				.setView(layout)
				.setPositiveButton(R.string.common_save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						final String password = passwordView.getText().toString();
						// Don't allow blank passwords
						if ("".equals(password)) {
							Util.toast(context, R.string.admin_change_password_invalid);
							return;
						}

						new SilentBackgroundTask<Void>(context) {
							@Override
							protected Void doInBackground() throws Throwable {
								MusicService musicService = MusicServiceFactory.getMusicService(context);
								musicService.changePassword(user.getUsername(), password, context, null);
								return null;
							}

							@Override
							protected void done(Void v) {
								Util.toast(context, context.getResources().getString(R.string.admin_change_password_success, user.getUsername()));
							}

							@Override
							protected void error(Throwable error) {
								String msg;
								if (error instanceof OfflineException || error instanceof ServerTooOldException) {
									msg = getErrorMessage(error);
								} else {
									msg = context.getResources().getString(R.string.admin_change_password_error, user.getUsername());
								}

								Util.toast(context, msg);
							}
						}.execute();
					}
				})
				.setNegativeButton(R.string.common_cancel, null)
				.setCancelable(true);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public static void updateSettings(final Context context, final User user) {
		new SilentBackgroundTask<Void>(context) {
			@Override
			protected Void doInBackground() throws Throwable {
				MusicService musicService = MusicServiceFactory.getMusicService(context);
				musicService.updateUser(user, context, null);
				return null;
			}

			@Override
			protected void done(Void v) {
				Util.toast(context, context.getResources().getString(R.string.admin_update_permissions_success, user.getUsername()));
			}

			@Override
			protected void error(Throwable error) {
				String msg;
				if (error instanceof OfflineException || error instanceof ServerTooOldException) {
					msg = getErrorMessage(error);
				} else {
					msg = context.getResources().getString(R.string.admin_update_permissions_error, user.getUsername());
				}

				Util.toast(context, msg);
			}
		}.execute();
	}

	public static void changeEmail(final Activity context, final User user) {
		View layout = context.getLayoutInflater().inflate(R.layout.change_email, null);
		final TextView emailView = (TextView) layout.findViewById(R.id.new_email);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.admin_change_email)
				.setView(layout)
				.setPositiveButton(R.string.common_save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						final String email = emailView.getText().toString();
						// Don't allow blank emails
						if ("".equals(email)) {
							Util.toast(context, R.string.admin_change_email_invalid);
							return;
						}

						new SilentBackgroundTask<Void>(context) {
							@Override
							protected Void doInBackground() throws Throwable {
								MusicService musicService = MusicServiceFactory.getMusicService(context);
								musicService.changeEmail(user.getUsername(), email, context, null);
								return null;
							}

							@Override
							protected void done(Void v) {
								Util.toast(context, context.getResources().getString(R.string.admin_change_email_success, user.getUsername()));
							}

							@Override
							protected void error(Throwable error) {
								String msg;
								if (error instanceof OfflineException || error instanceof ServerTooOldException) {
									msg = getErrorMessage(error);
								} else {
									msg = context.getResources().getString(R.string.admin_change_email_error, user.getUsername());
								}

								Util.toast(context, msg);
							}
						}.execute();
					}
				})
				.setNegativeButton(R.string.common_cancel, null)
				.setCancelable(true);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public static void deleteUser(final Context context, final User user, final ArrayAdapter adapter) {
		Util.confirmDialog(context, R.string.common_delete, user.getUsername(), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new SilentBackgroundTask<Void>(context) {
					@Override
					protected Void doInBackground() throws Throwable {
						MusicService musicService = MusicServiceFactory.getMusicService(context);
						musicService.deleteUser(user.getUsername(), context, null);
						return null;
					}

					@Override
					protected void done(Void v) {
						if(adapter != null) {
							adapter.remove(user);
							adapter.notifyDataSetChanged();
						}

						Util.toast(context, context.getResources().getString(R.string.admin_delete_user_success, user.getUsername()));
					}

					@Override
					protected void error(Throwable error) {
						String msg;
						if (error instanceof OfflineException || error instanceof ServerTooOldException) {
							msg = getErrorMessage(error);
						} else {
							msg = context.getResources().getString(R.string.admin_delete_user_error, user.getUsername());
						}

						Util.toast(context, msg);
					}
				}.execute();
			}
		});
	}

	public static void addNewUser(final Activity context, final SubsonicFragment fragment) {
		final User user = new User();
		user.addSetting("adminRole", false);
		user.addSetting("settingsRole", true);
		user.addSetting("downloadRole", false);
		user.addSetting("uploadRole", false);
		user.addSetting("coverArtRole", false);
		user.addSetting("commentRole", false);
		user.addSetting("podcastRole", false);
		user.addSetting("streamRole", true);
		user.addSetting("jukeboxRole", false);
		user.addSetting("shareRole", false);

		View layout = context.getLayoutInflater().inflate(R.layout.create_user, null);
		final TextView usernameView = (TextView) layout.findViewById(R.id.username);
		final TextView emailView = (TextView) layout.findViewById(R.id.email);
		final TextView passwordView = (TextView) layout.findViewById(R.id.password);
		final ListView listView = (ListView) layout.findViewById(R.id.settings_list);
		listView.setAdapter(new SettingsAdapter(context, user, true));

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.menu_add_user)
				.setView(layout)
				.setPositiveButton(R.string.common_save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						final String username = usernameView.getText().toString();
						// Don't allow blank emails
						if ("".equals(username)) {
							Util.toast(context, R.string.admin_change_username_invalid);
							return;
						}

						final String email = emailView.getText().toString();
						// Don't allow blank emails
						if ("".equals(email)) {
							Util.toast(context, R.string.admin_change_email_invalid);
							return;
						}

						final String password = passwordView.getText().toString();
						if ("".equals(password)) {
							Util.toast(context, R.string.admin_change_password_invalid);
							return;
						}

						user.setUsername(username);
						user.setEmail(email);
						user.setPassword(password);

						new SilentBackgroundTask<Void>(context) {
							@Override
							protected Void doInBackground() throws Throwable {
								MusicService musicService = MusicServiceFactory.getMusicService(context);
								musicService.createUser(user, context, null);
								return null;
							}

							@Override
							protected void done(Void v) {
								fragment.onRefresh();
								Util.toast(context, context.getResources().getString(R.string.admin_create_user_success));
							}

							@Override
							protected void error(Throwable error) {
								String msg;
								if (error instanceof OfflineException || error instanceof ServerTooOldException) {
									msg = getErrorMessage(error);
								} else {
									msg = context.getResources().getString(R.string.admin_create_user_error);
								}

								Util.toast(context, msg);
							}
						}.execute();
					}
				})
				.setNegativeButton(R.string.common_cancel, null)
				.setCancelable(true);

		AlertDialog dialog = builder.create();
		dialog.show();
	}
}