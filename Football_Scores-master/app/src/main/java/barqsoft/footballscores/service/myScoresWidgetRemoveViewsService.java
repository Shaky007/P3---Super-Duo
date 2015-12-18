package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by sjaakkallemein on 15/12/15.
 */
@TargetApi(11)
public class myScoresWidgetRemoveViewsService extends RemoteViewsService {

    private interface ScoresQuery {
        String[] COLUMNS = {
                DatabaseContract.scores_table.ID_COL,
                DatabaseContract.scores_table.DATE_COL,
                DatabaseContract.scores_table.TIME_COL,
                DatabaseContract.scores_table.HOME_COL,
                DatabaseContract.scores_table.AWAY_COL,
                DatabaseContract.scores_table.LEAGUE_COL,
                DatabaseContract.scores_table.HOME_GOALS_COL,
                DatabaseContract.scores_table.AWAY_GOALS_COL,
                DatabaseContract.scores_table.MATCH_ID,
                DatabaseContract.scores_table.MATCH_DAY,
        };
    }

    public static final int COL_ID = 0;
    public static final int COL_DATE = 1;
    public static final int COL_TIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_LEAGUE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_MATCH_ID = 8;
    public static final int COL_MATCH_DAY = 9;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor cursor = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {

                final long identity = Binder.clearCallingIdentity();

                Uri uri = DatabaseContract.scores_table.buildScoreWithDate();

                String todayDate = this.DateString(0);

                cursor = getContentResolver().query(uri, ScoresQuery.COLUMNS, null, new String[]{todayDate}, null);

                Binder.restoreCallingIdentity(identity);
            }

            protected String DateString(int daysBack) {
                Date fragmentdate = new Date(System.currentTimeMillis()-(daysBack*86400000));
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                return format.format(fragmentdate);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                if (cursor == null)
                    return  0;
                return cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                    cursor == null ||
                    !cursor.moveToPosition(position)) {
                    return null;
                }
                final RemoteViews views = new RemoteViews(getPackageName(), R.layout.scores_list_item);

                String score = Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS));

                views.setTextViewText(R.id.data_textview, cursor.getString(COL_TIME));
                views.setTextViewText(R.id.home_name, cursor.getString(COL_HOME));
                views.setTextViewText(R.id.away_name, cursor.getString(COL_AWAY));
                views.setTextViewText(R.id.score_textview, score);

                views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(cursor.getString(COL_HOME)));
                views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(cursor.getString(COL_AWAY)));

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (cursor.moveToPosition(position))
                    return cursor.getLong(1);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };

    }
}
