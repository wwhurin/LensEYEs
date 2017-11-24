package kr.hs.emirim.wwhurin.lenseye;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by KJH on 2017-05-15.
 * Fragment Life Style
 * 1. Fragment is added
 * 2. onAttach()                    Fragment가 Activty에 붙을때 호출
 * 3. onCreate()                    Activty에서의 onCreate()와 비슷하나, ui 관련 작업은 할 수 없다.
 * 4. onCreateView()                Layout을 inflater을 하여 View 작업을 하는 곳
 * 5. onActivityCreated()           Activity에서 Fragment를 모두 생성하고난 다음에 호출됨. Activty의 onCreate()에서 setContentView()한 다음과 같다
 * 6. onStart()                     Fragment가 화면에 표시될때 호출, 사용자의 Action과 상호 작용이 불가능함
 * 7. onResume()                    Fragment가 화면에 완전히 그렸으며, 사용자의 Action과 상호 작용이 가능함
 * 8. Fragment is active
 * 9. User navigates backward or fragment is removed/replaced  or Fragment is added to the back stack, then removed/replaced
 * 10. onPause()
 * 11. onStop()                     Fragment가 화면에서 더이상 보여지지 않게됬을때
 * 12. onDestroy()                  View 리소스를 해제할수있도록 호출. backstack을 사용했다면 Fragment를 다시 돌아갈때 onCreateView()가 호출됨
 * 13. onDetached()
 * 14. Fragment is destroyed
 */


/**
 * Google Map CallStack
 * 1. onCreate()
 * 2. onCreateView()
 * 3. onActivityCreated()
 * 4. onStart();
 * 5. onResume();
 * 5-2. onMapReady();
 * 6. onPause();
 * 7. onSaveInstanceState();
 * 8. onMapReady();
 */

public class GMapFragment extends Fragment
        implements OnMapReadyCallback ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 15000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 15000;

    private GoogleMap googleMap = null;
    private static GoogleMap gMap = null;
    private MapView mapView = null;
    private GoogleApiClient googleApiClient = null;
    private Marker currentMarker = null;

    private final static int MAXENTRIES = 5;
    private String[] LikelyPlaceNames = null;
    private String[] LikelyAddresses = null;
    private String[] LikelyAttributions = null;
    private LatLng[] LikelyLatLngs = null;

    private static Location currentLocation = null;
    private static LocationManager locationManager;
    private static android.location.LocationListener locationListener;
    private static JSONObject object;

    static Location userLocation;

    String urlString;
    Handler handler = new Handler();

    View layout;

    TextView et_webpage_src;
    static String whereT="";
    String address="";
    String Hospital="";

    public GMapFragment() {
        // required
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();





        if (location != null) {
            //현재위치의 위도 경도 가져옴
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);
//          markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            // BitmapDescriptorFactory 생성하기 위한 소스
            MapsInitializer.initialize(getContext());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

           /* markerOptions
                    .position(currentLocation)
                    .title("원하는 위치(위도, 경도)에 마커를 표시했습니다.");

            // 마커를 생성한다.*/
           // this.googleMap.addMarker(markerOptions);

            /*
            //카메라를 여의도 위치로 옮긴다.
            mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));*/


            currentMarker = gMap.addMarker(markerOptions);

            gMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));


            return;
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = this.googleMap.addMarker(markerOptions);

        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
    }
/*
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }*/

    public void checkMarks(){
        if (userLocation != null) {
            // TODO 위치를 처음 얻어왔을 때 하고 싶은 것
            double latitude = userLocation.getLatitude();
            double longitude = userLocation.getLongitude();
            Log.d("위치ㅣㅣㅣㅣㅣㅣㅣㅣㅣ: ", "위도 : " + latitude + ", 경도 : " + longitude);
            urlString="https://maps.googleapis.com/maps/api/place/nearbysearch/json?hl=ko&location="+latitude+","+longitude+"&radius=1000&type=%EB%B3%91%EC%9B%90&keyword=%EC%95%88%EA%B3%BC&key=AIzaSyBxd5b_mGO7pycvrX3-AsukTLBTYm7dkBQ&language=ko";
            //urlString="https://maps.googleapis.com/maps/api/place/nearbysearch/json?hl=ko&location="+latitude+","+longitude+"&radius=1000" +
            //        "&type=병원0&keyword=안과&key=AIzaSyBxd5b_mGO7pycvrX3-AsukTLBTYm7dkBQ";
            Log.d("체크:", ""+urlString);

        }

        new Thread() {
            public void run() {
                object = getTe();


                handler.post(new Runnable() {

                    public void run() {
                        try {

                            JSONArray getR = object.getJSONArray("results");
                            int numberOfItemsInResp = getR.length();

                            for(int i=0; i<numberOfItemsInResp; i++) {
                                JSONObject jsonObject = object.getJSONArray("results").getJSONObject(i);
                                Log.d("ㅇ{:::::", "" + jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));

                                et_webpage_src.setText("dkdjkjkjk:::::" + "" + jsonObject.getJSONObject("geometry")
                                        .getJSONObject("location").getDouble("lat")+"\n");

                                whereT+=jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat")+","
                                        +jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng")+"/";//값저장(근처 안과 위도경도)

                                Log.d("ㅇ{:::::", "" + whereT);

                                address+=jsonObject.getString("vicinity")+"/";
                                Log.d("ㅇ{::::!!!!:", "" + address);

                                Hospital+=jsonObject.getString("name")+"/";
                                Log.d("ㅇ{::::!!!!:", "" + Hospital);
                            }

                            String drec[]=whereT.split("/");
                            MarkerOptions[] m = new MarkerOptions[drec.length];
                            for(int i=0; i<drec.length; i++){
                                Log.d("되게해숮사ㅓㅈ딧", ""+i);

                                String getDrection[]=drec[i].split(",");
                                Log.d("아!!!!!!!!:", getDrection[1]);

                                String getadd[]=address.split("/");
                                String getHName[]=Hospital.split("/");

                                m[i]=new MarkerOptions();

                                m[i] // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                                        .position(new LatLng(Double.parseDouble(getDrection[0]), Double.parseDouble(getDrection[1])))
                                        .title(getHName[i]); // 타이틀.

                                // 2. 마커 생성 (마커를 나타냄)
                                gMap.addMarker(m[i]);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }.start();

    }

    private static ListView mlistView;
    ArrayList<HashMap<String, String>> mArrayList;
    private static  HashMap<String, String> hashMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_map, container, false);

        et_webpage_src = (TextView) layout.findViewById(R.id.textView);
        mapView = (MapView) layout.findViewById(R.id.map);
        mlistView = (ListView) layout.findViewById(R.id.listView);
        mArrayList = new ArrayList<>();

        mapView.getMapAsync(this);

        // LocationManager의 인스턴스와
        // 레이아웃 내 위짓의 인스턴스를 받아옵니다.
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // 사용자의 위치 수신을 위한 세팅 //
        settingGPS();

        // 사용자의 현재 위치 //
        userLocation  = getMyLocation();

        if (userLocation != null) {
            // TODO 위치를 처음 얻어왔을 때 하고 싶은 것
            double latitude = userLocation.getLatitude();
            double longitude = userLocation.getLongitude();
            Log.d("위치ㅣㅣㅣㅣㅣㅣㅣㅣㅣ: ", "위도 : " + latitude + ", 경도 : " + longitude);
            urlString="https://maps.googleapis.com/maps/api/place/nearbysearch/json?hl=ko&location="+latitude+","+longitude+"&radius=1000&type=%EB%B3%91%EC%9B%90&keyword=%EC%95%88%EA%B3%BC&key=AIzaSyBxd5b_mGO7pycvrX3-AsukTLBTYm7dkBQ&language=ko";
            //urlString="https://maps.googleapis.com/maps/api/place/nearbysearch/json?hl=ko&location="+latitude+","+longitude+"&radius=1000" +
            //        "&type=병원0&keyword=안과&key=AIzaSyBxd5b_mGO7pycvrX3-AsukTLBTYm7dkBQ";
            Log.d("체크:", ""+urlString);

        }

        new Thread() {
            public void run() {
                object = getTe();


                handler.post(new Runnable() {

                    public void run() {
                        try {

                            JSONArray getR = object.getJSONArray("results");
                            int numberOfItemsInResp = getR.length();

                            for(int i=0; i<numberOfItemsInResp; i++) {
                                JSONObject jsonObject = object.getJSONArray("results").getJSONObject(i);
                                Log.d("ㅇ{:::::", "" + jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));

                                et_webpage_src.setText("dkdjkjkjk:::::" + "" + jsonObject.getJSONObject("geometry")
                                        .getJSONObject("location").getDouble("lat")+"\n");

                                whereT+=jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat")+","
                                        +jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng")+"/";//값저장(근처 안과 위도경도)

                                Log.d("ㅇ{:::::", "" + whereT);

                                address+=jsonObject.getString("vicinity")+"/";
                                Log.d("ㅇ{::::!!!!:", "" + address);

                                Hospital+=jsonObject.getString("name")+"/";
                                Log.d("ㅇ{::::!!!!:", "" + Hospital);
                            }

                            String drec[]=whereT.split("/");
                            MarkerOptions[] m = new MarkerOptions[drec.length];
                            for(int i=0; i<drec.length; i++){
                                Log.d("되게해숮사ㅓㅈ딧", ""+i);

                                String getDrection[]=drec[i].split(",");
                                Log.d("아!!!!!!!!:", getDrection[1]);

                                String getadd[]=address.split("/");
                                String getHName[]=Hospital.split("/");

                                hashMap = new HashMap<>();

                                hashMap.put("name", getHName[i]);
                                hashMap.put("address", getadd[i]);
                                // hashMap.put(TAG_ADDRESS, address);

                                mArrayList.add(hashMap);

                                m[i]=new MarkerOptions();

                                m[i] // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                                        .position(new LatLng(Double.parseDouble(getDrection[0]), Double.parseDouble(getDrection[1])))
                                        .title(getHName[i]); // 타이틀.

                                // 2. 마커 생성 (마커를 나타냄)
                               // gMap.addMarker(m[i]);


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //리스트뷰생성!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        ListAdapter adapter = new SimpleAdapter(
                               layout.getContext(), mArrayList, R.layout.maplist,
                                new String[]{"name", "address"},
                                new int[]{R.id.textView_list_name, R.id.textView_list_address}
                        );

                        mlistView.setAdapter(adapter);


                    }
                });
            }
        }.start();


     /*   PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Location location = new Location("");
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);

                setCurrentLocation(location, place.getName().toString(), place.getAddress().toString());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });*/



        //Log.d("체크:", currentLocation.getLatitude()+" , "+currentLocation.getLongitude());


        return layout;
    }


    /**
     * GPS 를 받기 위한 매니저와 리스너 설정
     */
    private void settingGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        locationListener = new android.location.LocationListener() {
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // TODO 위도, 경도로 하고 싶은 것

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    /**
     * GPS 권한 응답에 따른 처리
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    boolean canReadLocation = false;

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MInteger.REQUEST_CODE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
// success!
                Location userLocation = getMyLocation();
                if (userLocation != null) {
// 다음 데이터 //
                    // String lang = MString.getLangFromSystemLang(getActivity().getApplicationContext());
// todo 사용자의 현재 위치 구하기
                    double latitude = userLocation.getLatitude();
                    double longitude = userLocation.getLongitude();
                }
                canReadLocation = true;
            } else {
// Permission was denied or request was cancelled
                canReadLocation = false;
            }
        }
    }


    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, MInteger.REQUEST_CODE_LOCATION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) locationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
                //mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lng, lat), true);
                //setCurrentLocation(currentLocation, "사용자 집", "집!");
                Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
            }
        }
        return currentLocation;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

        if (googleApiClient != null && googleApiClient.isConnected())
            googleApiClient.disconnect();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (googleApiClient != null)
            googleApiClient.connect();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();

        if (googleApiClient != null) {
            googleApiClient.unregisterConnectionCallbacks(this);
            googleApiClient.unregisterConnectionFailedListener(this);

            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                googleApiClient.disconnect();
            }
        }
    }
/*

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
//        fragmentTransaction.commit();
    }
*/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //액티비티가 처음 생성될 때 실행되는 함수
        MapsInitializer.initialize(getActivity().getApplicationContext());

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // OnMapReadyCallback implements 해야 mapView.getMapAsync(this); 사용가능. this 가 OnMapReadyCallback

        this.googleMap = googleMap;
        gMap=googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에 지도의 초기위치를 서울로 이동
       // setCurrentLocation(null, "위치정보 가져올 수 없음", "위치 퍼미션과 GPS 활성 여부 확인");

        //나침반이 나타나도록 설정
        googleMap.getUiSettings().setCompassEnabled(true);
        // 매끄럽게 이동함
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        setCurrentLocation(userLocation, "사용자 집", "집!");

        //  API 23 이상이면 런타임 퍼미션 처리 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 사용권한체크
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION);

            if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                //사용권한이 없을경우
                //권한 재요청
                ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                //사용권한이 있는경우
                if (googleApiClient == null) {
                    buildGoogleApiClient();
                }

                if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        } else {

            if (googleApiClient == null) {
                buildGoogleApiClient();
            }

            googleMap.setMyLocationEnabled(true);
        }


    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();
        googleApiClient.connect();
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!checkLocationServicesStatus()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("위치 서비스 비활성화");
            builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" +
                    "위치 설정을 수정하십시오.");
            builder.setCancelable(true);
            builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent callGPSSettingIntent =
                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.create().show();
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationServices.FusedLocationApi
                        .requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        } else {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(googleApiClient, locationRequest, this);

            this.googleMap.getUiSettings().setCompassEnabled(true);
            this.googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Location location = new Location("");
        location.setLatitude(DEFAULT_LOCATION.latitude);
        location.setLongitude((DEFAULT_LOCATION.longitude));

        setCurrentLocation(location, "위치정보 가져올 수 없음",
                "위치 퍼미션과 GPS활성 여부 확인");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged call..");
       // searchCurrentPlaces();
        userLocation  = getMyLocation();
        checkMarks();
    }

    private void searchCurrentPlaces() {
        @SuppressWarnings("MissingPermission")
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(googleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {

            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                int i = 0;
                LikelyPlaceNames = new String[MAXENTRIES];
                LikelyAddresses = new String[MAXENTRIES];
                LikelyAttributions = new String[MAXENTRIES];
                LikelyLatLngs = new LatLng[MAXENTRIES];

                for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                    LikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                    LikelyAddresses[i] = (String) placeLikelihood.getPlace().getAddress();
                    LikelyAttributions[i] = (String) placeLikelihood.getPlace().getAttributions();
                    LikelyLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                    i++;
                    if (i > MAXENTRIES - 1) {
                        break;
                    }
                }

                placeLikelihoods.release();

                Location location = new Location("");
                location.setLatitude(LikelyLatLngs[0].latitude);
                location.setLongitude(LikelyLatLngs[0].longitude);

                setCurrentLocation(location, LikelyPlaceNames[0], LikelyAddresses[0]);
            }
        });

    }

    public JSONObject getTe() {
        //출력 영역
        URL url;
        HttpURLConnection urlConnection = null;
        BufferedInputStream buf = null;
        try {
            //[URL 지정과 접속]
            url=new URL(urlString);
            Log.d("되라", urlString);
            //웹서버 URL 지정
            //url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?hl=ko&location=37.46632156230054,126.93250416627359&radius=500&type=%EB%B3%91%EC%9B%90&keyword=%EC%95%88%EA%B3%BC&key=AIzaSyBxd5b_mGO7pycvrX3-AsukTLBTYm7dkBQ");
            //url=new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?hl=ko&location=37.4762706,126.8622115&radius=500&type=%EB%B3%91%EC%9B%90&keyword=%EC%95%88%EA%B3%BC&key=AIzaSyBxd5b_mGO7pycvrX3-AsukTLBTYm7dkBQ");
            //URL 접속
            // urlConnection = (HttpURLConnection) url.openConnection();

            //[웹문서 소스를 버퍼에 저장]
            //데이터를 버퍼에 기록

            //buf = new BufferedInputStream(urlConnection.getInputStream());
            //BufferedReader bufreader = new BufferedReader(new InputStreamReader(buf,"UTF-8"));

            /*BufferedReader bufreader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            Log.d("라인!!!!!!:",bufreader.toString());*/

            InputStream is = url.openConnection().getInputStream();
            StringBuffer buffer = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = null;
            String page = "";

            // StringBuffer buffer=new StringBuffer();

            //버퍼의 웹문서 소스를 줄단위로 읽어(line), Page에 저장함
            while ((line = br.readLine()) != null) {
                Log.d("라인쓰:", line);
                page += line;
                buffer.append(line + "\n");
            }

            String data = buffer.toString();

            String flag = "start";
            if (data.startsWith("start")) {
                data = data.split("start")[1];
            } else {
                //data = data.split("destination")[1];
                flag = "destination";
            }

            JSONObject jsonData = new JSONObject(data);

            /*switch (jsonData.getString("status")){
                case "OK":*/
            JSONObject jsonObject = jsonData.getJSONArray("results").getJSONObject(0);
            Log.d("오키:::::", "" + jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));

            JSONObject jsonObject2 = jsonData.getJSONArray("results").getJSONObject(1);
            Log.d("오키2:::::", "" + jsonObject2.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));

            return jsonData;

            // }


          /*  //읽어들인 JSON포맷의 데이터를 JSON객체로 변환
            JSONObject json = new JSONObject(page);

            //ksk_list 에 해당하는 배열을 할당
            JSONArray jArr = json.getJSONArray("ksk_list");

            //배열의 크기만큼 반복하면서, name과 address의 값을 추출함
            for (int i=0; i<jArr.length(); i++){

                //i번째 배열 할당
                json = jArr.getJSONObject(i);

                //ksNo,korName의 값을 추출함
                String ksNo = json.getString("name");
                String korName = json.getString("lat");
               Log.d("!~~~~`: ", "ksNo:"+ksNo+"/korName:"+korName);

                //ksNo,korName의 값을 출력함
                et_webpage_src.append("[ "+ksNo+" ]\n");
                et_webpage_src.append(korName+"\n");
                et_webpage_src.append("\n");*/

            //}

        } catch (MalformedURLException e) {
            System.err.println("Malformed URL");
            e.printStackTrace();
            return null;
        } catch (JSONException e) {

            System.err.println("JSON parsing error");

            e.printStackTrace();
            return null;
        } catch (IOException e) {

            System.err.println("URL Connection failed");

            e.printStackTrace();
            return null;
        }
    }
}
