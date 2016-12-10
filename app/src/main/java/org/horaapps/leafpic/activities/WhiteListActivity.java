package org.horaapps.leafpic.activities;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.model.base.ImageFileFilter;
import org.horaapps.leafpic.util.StringUtils;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 01/04/16.
 */
public class WhiteListActivity extends SharedMediaActivity {


    private int REQUEST_CODE_SD_CARD_PERMISSIONS = 42;
    private FloatingActionButton fabWHDone;

    ArrayList<Item> folders = new ArrayList<>();
    ArrayList<String> alreadyTracked;

    RecyclerView mRecyclerView;
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_list);
        toolbar = (Toolbar) findViewById(org.horaapps.leafpic.R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(org.horaapps.leafpic.R.id.excluded_albums);
        fabWHDone = (FloatingActionButton) findViewById(R.id.fab_whitelist_done);
        initUi();
        alreadyTracked = getAlbums().getTrackedPaths();
        lookForFoldersInMediaStore();
    }


    private void lookForFoldersInMediaStore() {
        String[] projection = new String[]{
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA
        };

        String selection, selectionArgs[];

        selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=? or " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " ) GROUP BY ( " + MediaStore.Files.FileColumns.PARENT + " ";

        selectionArgs = new String[]{
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
        };
        Cursor cur = getContentResolver().query(
                MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, null);

        if (cur != null) {
            int idColumn = cur.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
            int nameColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int mediaColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);

            while (cur.moveToNext()) {
                Item item = new Item(cur.getLong(idColumn),
                        StringUtils.getBucketPathByImagePath(cur.getString(mediaColumn)),
                        cur.getString(nameColumn));
                int indexOf = alreadyTracked.indexOf(item.path);
                if (indexOf != -1) {
                    item.included = true;
                    alreadyTracked.remove(indexOf);
                }
                folders.add(item);
            }
            cur.close();
        }
    }

    @TestOnly
    private void fetchFolders(File dir) {
//        if (!alreadyTracked.contains(dir)) {
//            if (isFolderWithMedia(dir))
//                folders.add(new Item(dir.getPath(), dir.getName()));
//            File[] foo = dir.listFiles(new NotHiddenFoldersFilter());
//            if (foo != null)
//                for (File f : foo)
//                    if (!alreadyTracked.contains(f)) fetchFolders(f);
//        }
    }

    @TestOnly
    private boolean isFolderWithMedia(File dir) {
        String[] list = dir.list(new ImageFileFilter(true));
        return list != null && list.length > 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_track_albums, menu);
        //menu.findItem(R.id.action_done).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_done));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
            case R.id.action_done:
                tracker.handleItems(folders);
                finish();
                return true;
            */
           /* case R.id.action_show_music:
                Toast.makeText(this, "Fuck!", Toast.LENGTH_SHORT).show();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }


    private void initUi() {

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new ItemsAdapter());
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        fabWHDone.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_done).color(Color.WHITE));
        fabWHDone.setVisibility(View.VISIBLE);
        fabWHDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlbums().handleItems(folders);
                finish();
            }
        });
    }

    @Override
    public void updateUiElements(){
        toolbar.setBackgroundColor(getPrimaryColor());
        mRecyclerView.setBackgroundColor(getBackgroundColor());
        fabWHDone.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(R.string.chose_folders));
        findViewById(org.horaapps.leafpic.R.id.rl_ea).setBackgroundColor(getBackgroundColor());
    }


    public class Item {
        String path;
        String name;
        long id;
        boolean included = false;

        public String getPath() {
            return path;
        }

        public long getId() {
            return id;
        }

        public boolean isIncluded() {
            return included;
        }

        Item(long id, String path, String name) {
            this.path = path;
            this.name = name;
            this.id = id;
        }

        boolean toggleInclude() {
            included = !included;
            return included;
        }
    }

    private class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

        private View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.findViewById(R.id.folder_path).getTag();
                SwitchCompat s = (SwitchCompat) v.findViewById(R.id.tracked_status);
                s.setChecked(folders.get(pos).toggleInclude());
                setSwitchColor(getAccentColor(), s);
            }
        };

        public ItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_track_folder, parent, false);
            v.setOnClickListener(listener);
            return new ItemsAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ItemsAdapter.ViewHolder holder, final int position) {

            Item itm = folders.get(position);
            holder.path.setText(itm.path);
            holder.name.setText(itm.name);
            holder.path.setTag(position);
            holder.tracked.setChecked(itm.included);

            /**SET LAYOUT THEME**/
            holder.name.setTextColor(getTextColor());
            holder.path.setTextColor(getSubTextColor());
            holder.imgFolder.setColor(getIconColor());
            setSwitchColor(getAccentColor(), holder.tracked);
            holder.layout.setBackgroundColor(getCardBackgroundColor());
        }

        public int getItemCount() {
            return folders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layout;
            SwitchCompat tracked;
            IconicsImageView imgFolder;
            TextView name;
            TextView path;

            ViewHolder(View itemView) {
                super(itemView);
                layout = (LinearLayout) itemView.findViewById(R.id.linear_card_excluded);
                tracked = (SwitchCompat) itemView.findViewById(R.id.tracked_status);
                imgFolder = (IconicsImageView) itemView.findViewById(R.id.folder_icon);
                name = (TextView) itemView.findViewById(R.id.folder_name);
                path = (TextView) itemView.findViewById(R.id.folder_path);
            }
        }
    }
}