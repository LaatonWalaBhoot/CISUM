package home.cisum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class LibraryFragment extends Fragment{

    HashMap<Integer, NowPlayingActivity.Details> hashMap = new HashMap<>();
    HashMap<Integer, NowPlayingActivity.Details> NewDetails = new HashMap<>();
    hashmapAdapter adapter;
    ListView library;
    TextView name;
    Button dots;
    Button play_next_btn;
    Button add_to_playlist;
    Button share;
    Button delete;

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        NewDetails.clear();
        hashMap.clear();

        library = (ListView)view.findViewById(R.id.library);
        Bundle b = getActivity().getIntent().getExtras();
        if(b!=null) {
            hashMap = (HashMap<Integer, NowPlayingActivity.Details>) b.getSerializable("Details");
        }
        populate1(hashMap);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.search_view, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        final SearchView searchViewAndroidActionBar = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchViewAndroidActionBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchViewAndroidActionBar.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private class hashmapAdapter extends BaseAdapter implements Filterable {
        HashMap<Integer, NowPlayingActivity.Details> newHashMap = new HashMap<>();

        private hashmapAdapter(HashMap<Integer, NowPlayingActivity.Details> hashMap) {
            if(NewDetails.size()==0) {
                NewDetails.putAll(hashMap);
            }
        }

        @Override
        public int getCount() {
            return NewDetails.size();
        }

        @Override
        public Object getItem(int position) {
            return NewDetails.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final View result;

            if(convertView == null) {
                result = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item,parent,false);
            } else {
                result = convertView;
            }

            ((ImageView)result.findViewById(R.id.art_work)).setImageBitmap(NewDetails.get(position).getImage());
            ((TextView)result.findViewById(R.id.Name)).setText(NewDetails.get(position).getName());
            ((TextView)result.findViewById(R.id.artist)).setText(NewDetails.get(position).getArtist());
            ((TextView)result.findViewById(R.id.duration)).setText(NewDetails.get(position).getDuration()+" || ");

            dots = (Button)result.findViewById(R.id.dots);
            dots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    name = (TextView)result.findViewById(R.id.Name);

                    LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popup_view = inflater.inflate(R.layout.option_layout,null);
                    RelativeLayout item = (RelativeLayout) popup_view.findViewById(R.id.popup);

                    final float width= (getResources().getDimension(R.dimen.width_entry_in_dp));
                    final float height= getResources().getDimension(R.dimen.height_entry_in_dp);
                    final PopupWindow popupWindow  = new PopupWindow(popup_view,Math.round(width),Math.round(height),true);

                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    popupWindow.showAtLocation(item, Gravity.END, 0, 0);
                    popupWindow.showAsDropDown(dots);

                    play_next_btn = (Button)popup_view.findViewById(R.id.play_next);
                    add_to_playlist = (Button)popup_view.findViewById(R.id.add_to_playlist);
                    share = (Button)popup_view.findViewById(R.id.share);
                    delete = (Button)popup_view.findViewById(R.id.delete);

                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for(int i=0;i<NewDetails.size();i++) {
                                if(NewDetails.get(i).getName().equals(name.getText().toString())) {
                                    for(int j=0;j<hashMap.size();j++) {
                                        if(hashMap.get(j).getName().equals(name.getText().toString())) {
                                            if (true) {
                                                //scanaddedFile(hashMap.get(j).getPath());
                                                NewDetails.remove(i);
                                                hashMap.remove(j);
                                                populate1(NewDetails);
                                            }
                                        }
                                    }
                                }
                            }
                            popupWindow.dismiss();
                        }
                    });

                }
            });

            return result;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                int count =0;
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults results = new FilterResults();
                    if(constraint == null || constraint.length() == 0) {
                        results.values = hashMap;
                        results.count = hashMap.size();
                    }
                    else {
                        String filterString = constraint.toString().toLowerCase();

                        for(int i =0;i<hashMap.size();i++) {
                            if(hashMap.get(i).getName().toLowerCase().contains(filterString)) {
                                Bitmap Image = hashMap.get(i).getImage();
                                String ID = hashMap.get(i).getID();
                                String name = hashMap.get(i).getName();
                                String path = hashMap.get(i).getPath();
                                String artist = hashMap.get(i).getArtist();
                                String duration = hashMap.get(i).getDuration();
                                newHashMap.put(count++,new NowPlayingActivity.Details(Image,ID,name,path,artist,duration));
                            }
                        }
                        results.values = newHashMap;
                        results.count = newHashMap.size();
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if(results!=null) {
                        if(results.count>0) {
                            NewDetails.clear();
                            NewDetails.putAll((HashMap<Integer, NowPlayingActivity.Details>)results.values);
                        } else {
                            NewDetails.clear();
                        }
                    }
                    populate1(NewDetails);
                }
            };
        }
    }

    private void scanaddedFile(String path) {
        try {
            MediaScannerConnection.scanFile(getContext(), new String[] { path },
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            getActivity().getContentResolver().delete(uri, null, null);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void populate1(HashMap<Integer,NowPlayingActivity.Details> hashMap) {

        adapter = new hashmapAdapter(hashMap);
        library.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        library.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                name = (TextView)view.findViewById(R.id.Name);
                String SongName = name.getText().toString();
                Intent intent = new Intent(getActivity().getApplicationContext(),NowPlayingActivity.class);
                intent.putExtra("SongName",SongName);
                getActivity().setResult(555,intent);
                getActivity().finish();
            }
        });
    }
}
