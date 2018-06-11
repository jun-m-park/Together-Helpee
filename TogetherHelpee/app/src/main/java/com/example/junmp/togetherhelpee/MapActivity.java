package com.example.junmp.togetherhelpee;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener  {
    String phone_num;
    String helperId;
    int volunteerId;
    String helperName;

    double latitude;
    double longitude;

    putLoc putLoc;
    getHelper getHelper;
    String mJsonString;

    private GoogleMap mMap;
    private TextView title;
    Geocoder geocoder;
    Location address_loc;
    String address;
    List<Address> address_list;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private String provider;

    Marker myMarker;
    ArrayList<Marker> markerArrayList = new ArrayList<>();

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = true;
    private GpsInfo gps;

    private ImageView refresh;
    private Button btn_stop;

    private int searchFlag=1;

    private double latitude_my;
    private double longitude_my;
    private LatLng myplace;

    SupportMapFragment mapFragment;

    Timer timer = new Timer();
    TimerTask refresh_location = new TimerTask() {
        public void run() {
            provider = LocationManager.GPS_PROVIDER;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                checkMyPermissionLocation();
            } else {
                initGoogleMapLocation();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_map);

        refresh = (ImageView)findViewById(R.id.refresh);
        btn_stop = findViewById(R.id.btn_stop);

        provider = LocationManager.GPS_PROVIDER;

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MapActivity.this, "권한 거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.INTERNET,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();

        Intent from = getIntent();

        if(from.getStringExtra("phonenum")!=null){
            phone_num = from.getStringExtra("phonenum");
        }
        if(from.getStringExtra("helperId")!=null){
            helperId = from.getStringExtra("helperId");
        }
        volunteerId = from.getIntExtra("volunteerId",0);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        timer.schedule(refresh_location, 0, 5000);

        refresh.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toStart = new Intent(MapActivity.this, Call1Activity.class);
                toStart.putExtra("from",1);
                startActivity(toStart);
                finish();
            }
        });
    }

    private class getHelper extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result.equals("[]")){
                Intent toError = new Intent(MapActivity.this, ErrorActivity.class);
                startActivity(toError);
                finish();
            }
            else {
                mJsonString = result;
                try {
                    showResult();
                } catch (JSONException e) {
                    Log.d("fda","asd");
                    e.printStackTrace();
                }
            }
        }


        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0]+helperId;
            Log.d("Asd", serverURL);
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("fadsfsads", "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d("fadsfsads", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }
    private void showResult() throws JSONException {
        try {
            JSONArray jsonArray = new JSONArray(mJsonString);

            for(int i=jsonArray.length()-1;i>=0;i--){
                JSONObject item = jsonArray.getJSONObject(i);

                latitude = item.getDouble("latitude");
                longitude = item.getDouble("longitude");

                String name = item.getString("name");
                helperName = name;
                btn_stop.setText(name+"학생을 만났다면 눌러주세요");


                // Obtain the SupportMapFragment and get notified when the map is ready to be used.

            }

        } catch (JSONException e) {
            Log.d("fadsfsads", "showResult : ", e);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = null;
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        getHelper = new getHelper();
        getHelper.execute("http://210.89.191.125/helpee/helper/name/");

        LatLng startplace = new LatLng(latitude_my, longitude_my);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startplace, 30));
        mMap.setOnMarkerClickListener(this);
    }

    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
/*
        for(int i =0; i<markerArrayList.size();i++)
        {
            if(marker.getTag().equals(markerArrayList.get(i).getTag()))
            {
                Intent intent = new Intent(getApplicationContext(),RegisterHelp_popup.class);
                intent.putExtra("helpeeid",marker.getTitle());
                HelpMarker helpMarker = helpMarkers.get(i);
                Help help = helpMarker.getHelp();
                intent.putExtra("help",help);
                startActivity(intent);
            }
        }
*/
       /* for(int i =0; i<helpMarkers.size();i++)
        {
            if(marker.getTag().equals(helpMarkers.get(i).getMarker().getTag()))
            {
                Intent intent = new Intent(getApplicationContext(),RegisterHelp_popup.class);
                intent.putExtra("helpeeid",marker.getTitle());
                HelpMarker helpMarker = helpMarkers.get(i);
                Help help = helpMarker.getHelp();
                intent.putExtra("help",help);
                intent.putExtra("helper",HELPER_ME);
                intent.putExtra("searchflag",searchFlag);
                startActivity(intent);
            }
        }*/


        return false;
    }


    private void checkMyPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission Check
            PermissionSettingUtils.requestPermission(this);
        } else {
            provider = LocationManager.GPS_PROVIDER;
            initGoogleMapLocation();
        }
    }

    private void initGoogleMapLocation() {
        getHelper = new getHelper();
        getHelper.execute("http://210.89.191.125/helpee/helper/name/");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        /**
         * Location Setting API를
         */
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        /*
         * 위치정보 결과를 리턴하는 Callback
         */
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                super.onLocationResult(result);
                //mCurrentLocation = locationResult.getLastLocation();
                mMap.clear();

                mCurrentLocation = result.getLocations().get(0);

                latitude_my = mCurrentLocation.getLatitude();
                longitude_my = mCurrentLocation.getLongitude();

                putLoc = new putLoc();
                putLoc.execute("http://210.89.191.125/helpee/real-matching/location");

                Log.d("Fdasfsaf",latitude_my+"");
                Log.d("Fdasfsaf",longitude_my+"");


                myplace = new LatLng(latitude_my, longitude_my);

                Marker mymarker;

                mymarker =mMap.addMarker(new MarkerOptions()
                        .position(myplace)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .title("내 위치"));
                mymarker.showInfoWindow();

                Log.d("qwe","qwe");
                Log.d("loc", String.valueOf(mCurrentLocation.getAltitude()));

                String helpeeid = helperId;
                LatLng place = new LatLng(latitude,longitude);

                Marker marker;
                MarkerOptions options = new MarkerOptions();

                marker =mMap.addMarker(new MarkerOptions()
                        .position(place)
                        .title(helperName+":"+helpeeid));
                marker.showInfoWindow();



                //  myMarker =mMap.addMarker(new MarkerOptions()
                //          .position(myplace)
                //          .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                //          .title("내 위치"));
                //  myMarker.setTag(1000);
                //  myMarker.showInfoWindow();
                //    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myplace, 14));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myplace, 30));

                /**
                 * 지속적으로 위치정보를 받으려면
                 * mLocationRequest.setNumUpdates(1) 주석처리하고
                 * 밑에 코드를 주석을 푼다
                 */
                //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }

            //Location관련정보를 모두 사용할 수 있음을 의미
            @Override
            public void onLocationAvailability(LocationAvailability availability) {
                //boolean isLocation = availability.isLocationAvailable();
            }
        };
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        //여기선 한번만 위치정보를 가져오기 위함
        mLocationRequest.setNumUpdates(1);
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            //배터리소모에 상관없이 정확도를 최우선으로 고려
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }else{
            //배터리와 정확도의 밸런스를 고려하여 위치정보를 획득(정확도 다소 높음)
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        /**
         * 클라이언트가 사용하고자하는 위치 서비스 유형을 저장합니다. 위치 설정에도 사용됩니다.
         */
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);
        locationResponse.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.e("Response", "Successful acquisition of location information!!");
                //
                if (ActivityCompat.checkSelfPermission(MapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        });
        //위치 정보를 설정 및 획득하지 못했을때 callback
        locationResponse.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e("onFailure", "위치환경체크");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "위치설정체크";
                        Log.e("onFailure", errorMessage);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //요청코드가 맞지 않는다면
        if (requestCode != PermissionSettingUtils.REQUEST_CODE) {
            return;
        }
        if (PermissionSettingUtils.isPermissionGranted(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION}, grantResults)) {
            //허락을 받았다면 위치값을 알아오는 코드를 진행
            initGoogleMapLocation();
        } else {
            Toast.makeText(this, "위치정보사용 허락을 하지않아 앱을 중지합니다", Toast.LENGTH_SHORT).show();
            //finish();
        }
    }

    /**
     * 위치정보 제거
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }


    public boolean checkGPSService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            Intent intent = new Intent(this, GPS_popup.class);
            startActivityForResult(intent, 1);

            return false;

        } else {
            initGoogleMapLocation();
            return true;
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            checkGPSService();
        }

    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        finish();
    }

    @Override
    public void onPause() {
        timer.cancel();
        timer.purge();
        refresh_location.cancel();
        refresh_location=null;
        timer = null;

        super.onPause();
    }

    public void onResume(){
        if(timer == null && refresh_location == null){
            timer = new Timer();
            refresh_location = new TimerTask() {
                public void run() {
                    provider = LocationManager.GPS_PROVIDER;
                    initGoogleMapLocation();
                }
            };
            timer.schedule(refresh_location, 0, 5000);
        }

        super.onResume();
    }

    class putLoc extends AsyncTask<String, Void, String> {
        RequestHandler rh = new RequestHandler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... params) {
            String UPLOAD_URL = params[0];
            HashMap<String, String> data = new HashMap<>();
            data.put("userId", phone_num);
            data.put("latitude", String.valueOf(latitude_my));
            data.put("longitude", String.valueOf(longitude_my));

            String result = rh.sendPutRequest(UPLOAD_URL, data);

            return result;
        }
    }
}
