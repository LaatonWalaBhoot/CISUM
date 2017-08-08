package home.cisum;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Aishwarya on 8/1/2017.
 */

public class NowPlayingActivity extends Activity implements MediaPlayer.OnCompletionListener{

    private MediaPlayer cisum = new MediaPlayer();
    AudioManager mAudioManager;
    ComponentName mReceiverComponent;
    HashMap<Integer, Details> hashmap=new HashMap<>();
    private Button shuffle_btn;
    private Button repeat_btn;
    private Button next_btn;
    int count=0;
    int click=0;
    private Button play_pause_btn;
    private Button library_btn;
    public int songIndex=0;
    public String SongName;
    public int flag = 0;
    Details path;
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nowplaying);

        mAudioManager =  (AudioManager) getSystemService(this.AUDIO_SERVICE);
        mReceiverComponent = new ComponentName(this,HardButtonReceiver.class);
        mAudioManager.registerMediaButtonEventReceiver(mReceiverComponent);

        cisum.setOnCompletionListener(this);
        populate();

        play_pause_btn = (Button)findViewById(R.id.play_pause);
        play_pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cisum.isPlaying()) {
                    if(cisum!=null) {
                        cisum.pause();
                        play_pause_btn.setText("PLAY");
                    }
                }
                else {
                    if(cisum!=null) {
                        if(songIndex==0 && flag ==0) {
                            path = hashmap.get(0);
                            playSong(path.getPath());
                            flag =1;
                            play_pause_btn.setText("PAUSE");
                        }
                        else {
                            cisum.start();
                            play_pause_btn.setText("PAUSE");
                        }
                    }
                }
            }
        });

        library_btn = (Button)findViewById(R.id.library);
        library_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Database_Activity.class);
                Bundle b = new Bundle();
                b.putSerializable("Details",hashmap);
                intent.putExtras(b);
                startActivityForResult(intent,100);
            }
        });

        shuffle_btn = (Button)findViewById(R.id.shuffle);
        shuffle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShuffle) {
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(),"SHUFFLE IS OFF",Toast.LENGTH_LONG).show();
                }
                else {
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(),"SHUFFLE IS ON",Toast.LENGTH_LONG).show();
                }
            }
        });

        repeat_btn = (Button)findViewById(R.id.repeat);
        repeat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRepeat) {
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(),"REPEAT IS OFF",Toast.LENGTH_LONG).show();
                }
                else {
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(),"REPEAT IS ON",Toast.LENGTH_LONG).show();
                }
            }
        });



        next_btn = (Button)findViewById(R.id.next);
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShuffle) {
                    Random rand1 = new Random();
                    songIndex = rand1.nextInt((hashmap.size() - 1) - 0 + 1) + 0;
                    path = hashmap.get(songIndex);
                    playSong(path.getPath());
                }
                else {
                    if(songIndex==0 && flag ==0) {
                        path = hashmap.get(0);
                        playSong(path.getPath());
                    }
                    else if(songIndex < (hashmap.size() - 1)){
                        path = hashmap.get(songIndex+1);
                        playSong(path.getPath());
                        songIndex = songIndex + 1;
                    }else{
                        path = hashmap.get(0);
                        // play first song
                        playSong(path.getPath());
                        songIndex = 0;
                    }
                }
                play_pause_btn.setText("PAUSE");
            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(cisum!=null){
            cisum.stop();
            cisum.reset();
            cisum.release();
            cisum = null;
        }
        mAudioManager.unregisterMediaButtonEventReceiver(mReceiverComponent);
    }

    public Integer getPositionByName(String name) {
        int position=0;
        for (int i=0;i<hashmap.size();i++) {
            if(hashmap.get(i).getName().equals(name)) {
                position = i;
            }
        }
        return position;
    }

    static class Details implements java.io.Serializable {
        Bitmap Image;
        String ID;
        String name;
        String path;
        String artist;
        String duration;

        public Details(Bitmap Image, String ID, String name, String path, String artist, String duration) {

            this.Image = Image;
            this.ID = ID;
            this.name = name;
            this.path = path;
            this.artist = artist;
            this.duration = duration;
        }
        public Bitmap getImage() { return Image; }

        public String getID() { return  ID; }

        public String getName() { return name; }

        public String getPath() {
            return path;
        }

        public String getArtist() {
            return artist;
        }

        public String getDuration() {
            return duration;
        }
    }

    public void populate() {

        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Albums.ALBUM_ID};
        String sort_order = MediaStore.Audio.Media.TITLE.toUpperCase() + " ASC";
        Cursor libCur = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, sort_order);

        if (libCur != null) {
            if (libCur.moveToFirst()) {
                int AlbumIDColumn = libCur.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int IDColumn = libCur.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int NameColumn = libCur.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                int TitleColumn = libCur.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int DataColumn = libCur.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int DurationCoulumn = libCur.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int ArtistColumn = libCur.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                File file = new File(libCur.getString(DataColumn));
                do {
                    if (libCur.getString(NameColumn).endsWith(".mp3")&& file.exists()) {
                        Long thisSongAlbumID = libCur.getLong(AlbumIDColumn);
                        String thisSongID = libCur.getString(IDColumn);
                        String thisSongTitle = libCur.getString(TitleColumn);
                        String thisSongData = libCur.getString(DataColumn);
                        String thisSongDuration = libCur.getString(DurationCoulumn);
                        String thisSongArtist = libCur.getString(ArtistColumn);
                        if (String.valueOf(thisSongDuration) != null) {
                            try {
                                Long time = Long.valueOf(thisSongDuration);
                                long seconds = time / 1000;
                                long minutes = seconds / 60;
                                seconds = seconds % 60;

                                if (seconds < 10) {
                                    thisSongDuration = String.valueOf(minutes) + ":0" + String.valueOf(seconds);
                                } else {
                                    thisSongDuration = String.valueOf(minutes) + ":" + String.valueOf(seconds);
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        } else {
                            thisSongDuration = "0";
                        }
                        hashmap.put(count++, new Details(getAlbumart(thisSongAlbumID),thisSongID,thisSongTitle, thisSongData, thisSongArtist, thisSongDuration));
                    }
                }while (libCur.moveToNext());
            }
        }
    }

    private Bitmap getAlbumart(Long albumId) {

    }

    private void playSong(String path) {

        Toast.makeText(getApplicationContext(),path,Toast.LENGTH_SHORT).show();

        try {
            cisum.reset();
            cisum.setDataSource(path);
            cisum.prepare();
            cisum.start();

        } catch(IOException e) {

            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 555){
            SongName = data.getStringExtra("SongName");
            songIndex = getPositionByName(SongName);
            // play selected song
            path = hashmap.get(songIndex);
            playSong(path.getPath());
            play_pause_btn.setText("PAUSE");
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // check for repeat is ON or OFF
        if(isRepeat){
            // repeat is on play same song again
            path = hashmap.get(songIndex);
            playSong(path.getPath());
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand2 = new Random();
            songIndex = rand2.nextInt((hashmap.size() - 1) - 0 + 1) + 0;
            path = hashmap.get(songIndex);
            playSong(path.getPath());
        } else{
            // no repeat or shuffle ON - play next song
            if(songIndex < (hashmap.size() - 1)){
                path = hashmap.get(songIndex+1);
                playSong(path.getPath());
                songIndex = songIndex + 1;
            }else{
                // play first song
                path = hashmap.get(0);
                playSong(path.getPath());
                songIndex = 0;
            }
        }
    }
}
