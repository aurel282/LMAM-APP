package com.example.aurel.lmam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ActMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, com.google.android.gms.location.LocationListener {

    GoogleMap googleMap;
    LatLng latLng;
    float zoomLevel = 16.0f; //This goes up to 21

    static final int REQUEST_IMAGE_CAPTURE = 1;

    String mCurrentPhotoPath = "";
    String PictureName = "";

    boolean isPicEmpty = true;

    private static final String TAG = "LMAM-ActMain";
    private final double MaxDistanceToSeeMessageInMeter = 100;
    public static dataApp AccData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Log.d(TAG, "GetMAp");
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment));
        mapFragment.getMapAsync(this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Fab: Send Text Message");
                SendMessageText();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        String UserEmail = AccData.GetFireBaseUser().getEmail();
        Log.d(TAG, "UsuerMail: " + UserEmail);
        TextView mEmailView = (TextView) header.findViewById(R.id.username);
        mEmailView.setText(UserEmail);

        AccData.setCurrentActivity(this);
        AccData.SetDbMessages();

    }


    @Override
    public void onMapReady(GoogleMap map) {

        Log.d(TAG, "onMapReady");
        googleMap = map;
        setUpMap();

    }

    public void setUpMap() {

        Log.d(TAG, "SetupMap");

        googleMap.setOnMarkerClickListener(MyMarkerClickListener);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Bad Permission Access Location");
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setTrafficEnabled(false);
        googleMap.setIndoorEnabled(false);
        googleMap.setBuildingsEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        latLng = GetActualLocation();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker)
            {
                // inflate view window
                View v = getLayoutInflater().inflate(R.layout.message_view, null);
                // set other views content
                ImageView ActIm = (ImageView) v.findViewById(R.id.picture);
                TextView title = (TextView) v.findViewById(R.id.viewtitle);
                TextView message = (TextView) v.findViewById(R.id.viewmessage);
                title.setText("Message with picture");
                message.setText(AccData.lstMessages.get((int) marker.getZIndex()).getMessageText());
                StorageReference storageRef;
                storageRef = FirebaseStorage.getInstance().getReference();
                String PicName = AccData.lstMessages.get((int) marker.getZIndex()).getPictureName();
                StorageReference ImputRef = storageRef.child("images/"+PicName);
                Context context = getApplicationContext();
                Glide.with(context)
                        .using(new FirebaseImageLoader())
                        .load(ImputRef)
                        .into(ActIm);

                // set image view like this:
                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
    }

    public void onResume()
    {
        super.onResume();

        Log.d(TAG, "On Resume");
        UpdateMarkerMessage();
        //myHandler.postDelayed(r, 250);

        //new MyAsyncTaskGetData().execute();


        if (!PictureName.equals(""))
        {
            SavePictureOnFireBase(PictureName);
            PictureName = "";
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)  // Handle navigation view item clicks here.
    {

        int id = item.getItemId();

        if (id == R.id.leave_message) {
            Log.d(TAG, "Leave Message");
            SendMessageText();
        } else if (id == R.id.leave_picture) {
            Log.d(TAG, "Leave Picture");
            dispatchTakePictureIntent();
        } else if (id == R.id.see_messages_near_me) {
            Log.d(TAG, "See Messages");
            UpdateMarkerMessage();
        } else if (id == R.id.disconnect) {
            Log.d(TAG, "Disconnect");

            AccData.Disconnect();
            OpenLoginActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void OpenLoginActivity() {
        Intent myIntent = new Intent(ActMain.this, ActLogin.class);
        ActMain.this.startActivity(myIntent);
    }


    public LatLng GetActualLocation() {
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        double latitude;
        double longitude;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Bad Permission Access Location");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the com.example.aurel.lmam.user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return new LatLng(0,0);
        }

        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria,true));
        if (location == null)
        {
            latitude = 0;
            longitude = 0;
            Log.d(TAG, "Error Location Location");
        }
        else
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        Log.d(TAG, "Get Location:" + latitude + "Lat, " + longitude + "Long");
        LatLng temp = new LatLng(latitude, longitude);
        return temp;
    }

    @Override
    public void onLocationChanged(Location location)
    {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            latLng = new LatLng(latitude, longitude);
            Log.d(TAG, "Update Location:" + latitude + "Lat, " + longitude + "Long");
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
            UpdateMarkerMessage();
    }

    public void SendMessageText()
    {
            final String[] m_Text = new String[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(ActMain.this);
            builder.setTitle("Enter your message:");
            final EditText input = new EditText(ActMain.this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text[0] = input.getText().toString();
                    if (m_Text[0] != "")
                    {
                        Log.d(TAG, "Send Message: Message Written: " + m_Text[0]);
                        String UserUid;
                        latLng = GetActualLocation();           // Update Actuel Location

                        if (AccData.GetFireBaseUser() != null)
                        {
                            UserUid = AccData.GetFireBaseUser().getUid();
                            message ActualMessage = new message(Calendar.getInstance().getTime(), m_Text[0], UserUid , new MyLatLng(latLng));
                            // Month are from 0 to 11           Year are subtracting by 1900
                            ActualMessage.SendToDataBase();
                        }
                        else
                        {
                            Log.d(TAG, "Authentification Null");
                        }

                    }


                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

    public void SendMessagePict(final String PictureName)
    {
        final String[] m_Text = new String[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(ActMain.this);
        builder.setTitle("Enter your message:");
        final EditText input = new EditText(ActMain.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text[0] = input.getText().toString();
                if (m_Text[0] != "")
                {
                    Log.d(TAG, "Send Message: Message Written: " + m_Text[0]);
                    String UserUid;
                    latLng = GetActualLocation();           // Update Actuel Location

                    if (AccData.GetFireBaseUser() != null)
                    {
                        UserUid = AccData.GetFireBaseUser().getUid();
                        message ActualMessage = new message(Calendar.getInstance().getTime(), m_Text[0], UserUid , new MyLatLng(latLng), PictureName);
                        // Month are from 0 to 11           Year are subtracting by 1900
                        ActualMessage.SendToDataBase();
                    }
                    else
                    {
                        Log.d(TAG, "Authentification Null");
                    }

                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d(TAG, "PhotoName:" + photoFile.getAbsoluteFile());
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                Log.d(TAG, "PhotoURI:" + photoFile.toURI().toString());

                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);



                startActivityForResult(takePictureIntent, 2);
                //startActivityForResult(new Intent(
                //                MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                //                MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile)),2); // REQUEST_IMAGE_CAPTURE

            }
        }
    }

    private File createImageFile() throws IOException
    {
        // Create an image file name
        String UserUid = AccData.GetFireBaseUser().getUid();
        Log.d(TAG, "UID:" + UserUid);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_" + UserUid;
        Log.d(TAG, "file:" + imageFileName);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

            // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getPath();
        PictureName = image.getName();
        return image;
    }

    public void UpdateMarkerMessage()
    {
            if(!AccData.lstMessages.isEmpty())
            {
                googleMap.clear();
                MarkerOptions NewMarker = new MarkerOptions();
                for(int i = 0; i < AccData.lstMessages.size(); i++)
                {
                    NewMarker.position(AccData.lstMessages.get(i).getMessageLocation().GetLatlng());
                    NewMarker.title("Message");
                    NewMarker.visible(true);

                    NewMarker.zIndex(i);
                    googleMap.addMarker(NewMarker);

                    Log.d(TAG, "Add Marker: " + i + "Lat;"  +AccData.lstMessages.get(i).getMessageLocation().GetLatlng().toString());

                }
            }
            else
            {
                Log.d(TAG, "No messages found");
            }

        }

    OnMarkerClickListener MyMarkerClickListener = new OnMarkerClickListener()
    {
        @Override
        public boolean onMarkerClick(Marker marker)
        {
            Log.d(TAG, "Show Message");
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Log.d(TAG, "Show Message Dist :  " + new MyLatLng(marker.getPosition()).getDistanceInMeters(new MyLatLng(latLng)));
            if (new MyLatLng(marker.getPosition()).getDistanceInMeters(new MyLatLng(latLng)) < MaxDistanceToSeeMessageInMeter)
            {
                if(AccData.lstMessages.get((int) marker.getZIndex()).getHasPicture())
                {
                    marker.showInfoWindow();
                }
                else
                {
                    Toast toast = Toast.makeText(context, AccData.lstMessages.get((int) marker.getZIndex()).getMessageText(), duration);
                    toast.show();
                }
            }
            else
            {
                String ActText = "You're too far from the message to see it.";
                Toast toast = Toast.makeText(context, ActText, duration);
                toast.show();
            }
            return true;
        }
    };


    public void SavePictureOnFireBase(final String PicName)
    {
        String filepath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        File file = new File(filepath + "/" + PicName );
        if (file.exists())
        {
            Log.d(TAG, "File exist");
            file.mkdirs();
            Log.d(TAG, "File path:" + file.getPath());
            Log.d(TAG, "File Size:" + file.length());
        }
        else
        {
            Log.d(TAG, "File not exist exist");
            return;
        }

        StorageReference storageRef;
        storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference ImputRef = storageRef.child("images/"+PicName);
        ImputRef.putFile(Uri.fromFile(file))
                .addOnFailureListener(new OnFailureListener()
                {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                Log.d(TAG, "File not loaded");
            }
        })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG, "File loaded");
                SendMessagePict(PicName);
            }
        });
    }

    /*
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    */


}
