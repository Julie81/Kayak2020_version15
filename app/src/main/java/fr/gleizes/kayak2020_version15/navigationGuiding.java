package fr.gleizes.kayak2020_version15;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class navigationGuiding extends AppCompatActivity {
    List list = new ArrayList();
    TextView textView;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    TextView latTextView, lonTextView, listeDePosition, Position;
    //ArrayList<Location> position;  //teste de creation liste
    List<Double> position = new ArrayList<Double>();  // liste fonctionnelle
    List<Double> enregistrement_position = new ArrayList<Double>();  // liste fonctionnelle
    List<Double> enregistrement_angle = new ArrayList<Double>();  // liste fonctionnelle
    List<Double> angle = new ArrayList<Double>(); //angle par rapport au nord ou pas
    double ER1 = 6378250;  // grand rayon de la terre
    double ER2 = 6356515;  // petit rayon de la terre
    double deg2rad = Math.PI/180;  //transformer la carte a plat
    double latYmaison = 43.144927; //*ER1*deg2rad;
    double longXmaison = 6.078565; //*ER2*deg2rad;
    int Xavance =3;  //valeur initiale, 1 = positive, 2 = negative
    int Yavance =3;  //valeur initiale, 1 = positive, 2 = negative

    private Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_guiding);
        textView = findViewById(R.id.textView2);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        XMLReader();

        getLastLocation();

        XMLReader();
        /*Intent intent =getIntent();
        if (intent != null){
            list = null;
            if (intent.hasExtra("lalist")){
                list = intent.getParcelableArrayListExtra("lalist");
                textView.setText(String.valueOf(list.get(3)));
            }
        }*/
        // int test = intent.getIntExtra("teste",0);
        //textView.setText(String.valueOf(test));
        //textView.setText(String.valueOf(list.get(0)));

        //on initialise le timer
        if (myHandler==null){
            myHandler = new Handler();
            myHandler.postDelayed(myRunnable, 0);
        }

    }
    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();

                                requestNewLocationData();

                            }
                        }
                );

            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);  //temps d'intervalle de mise à jour de la localisation en ms
        mLocationRequest.setFastestInterval(100);
        //mLocationRequest.setNumUpdates(5000);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            // une theorie qui me vient a linstant : si on supprime la distance du point dobjectif on le ramene a 0 et on simplifie les calculs


            Location mLastLocation = locationResult.getLastLocation();
            //textView.setText(mLastLocation.getLatitude() + "");
            if (position.size()==4){
                angle.add(latYmaison-position.get(0));  //Ecart en Y a avancer + information sur le signe
                angle.add(longXmaison-position.get(1)); //Ecart en X a avancer + information sur le signe
                angle.add(position.get(2)-position.get(0)); // avancement ou reculement sur Y
                angle.add(position.get(3)-position.get(1)); // avancement ou reculement sur X

                //distance entre nous N-1 et lobectif
                angle.add(Math.sqrt((latYmaison-position.get(0))*(latYmaison-position.get(0))+(longXmaison-position.get(1))*(longXmaison-position.get(1))));
                //distance entre nouvelle pos et objectif
                angle.add(Math.sqrt((latYmaison-position.get(2))*(latYmaison-position.get(2))+(longXmaison-position.get(3))*(longXmaison-position.get(3))));
                angle.add(angle.get(4)-angle.get(5));
                if (angle.get(4)>angle.get(5)) {textView.setText("on sapproche");} else {textView.setText("on seloigne");}
                Position.setText(angle.get(4) + "grosse pute"  + angle.get(5) + "  ok" + angle.get(6));
                if (angle.get(2)>0){ Yavance = 1; } else {Yavance = 2;}  //savoir  ou est lobjectif en Y
                if (angle.get(3)>0){ Xavance = 1; } else {Xavance = 2;}  //savoir  ou est lobjectif en X

                //on va vers le nord = augmente la lat
                // on va vers l'est = on augmente la long
                // exemple : nord est = ++           sud ouest = - -

                if (Yavance==1 && Xavance ==1){listeDePosition.setText("nord est");}
                if (Yavance==2 && Xavance ==2){listeDePosition.setText("sud ouest");}
                if (Yavance==1 && Xavance ==2){listeDePosition.setText("nord ouest");}
                if (Yavance==2 && Xavance ==1){listeDePosition.setText("sud est");}
                position.remove(0); //vide lancienne latt
                position.remove(0); //vide lancienne long
                angle.clear();         //nettoyage de la liste de calcul
                Yavance = 3;
                Xavance =3;  //remise en condition initiale des variables
                listeDePosition.setText( angle.get(0)+" ");
                angle.remove(0);  //vide la liste angle
            }
        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        XMLReader();

        //on arrete le timer
        if (myHandler != null) {
            myHandler.removeCallbacks(myRunnable);
            myHandler=null;
        }
    }

    //on crée la fonction qui sera appelée par le timer
    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            //on enregistre les coordonnées
            //System.out.println(position.get(0));
            //System.out.println(position.get(1));
            //on enregistre les directions

            //on enregistre les vibrations
            System.out.println('a');
            myHandler.postDelayed(this, 4000);
        }
    };

    protected void XMLReader() {

        final DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();

        try {
            final DocumentBuilder builder = factory1.newDocumentBuilder();

            //on récupère le document qui contient toutes les données longitude et latitude
            final Document document = builder.parse(new File(Environment.getExternalStorageDirectory() + "/Android/data/"
                    + getPackageName() + "/balise1.xml"));
            System.out.println(document.getXmlVersion());

            final Element racine = document.getDocumentElement();
            System.out.println(racine.getNodeName());

            final NodeList racineNoeuds = racine.getChildNodes();
            final int nbRacineNoeuds = racineNoeuds.getLength();

            for (int i=0; i<nbRacineNoeuds; i++) {
                if (racineNoeuds.item(i).getNodeType()== Node.ELEMENT_NODE)
                {
                    final Element balise = (Element) racineNoeuds.item(i);
                    System.out.println(balise.getNodeName());
                    System.out.println("id : " +balise.getAttribute("id"));

                    final Element longitude = (Element) balise.getElementsByTagName("longitude").item(0);
                    System.out.println(longitude.getTextContent());
                    double longi = Double.valueOf(longitude.getTextContent());

                    final Element latitude = (Element)balise.getElementsByTagName("latitude").item(0);
                    System.out.println((latitude.getTextContent()));
                    double lat = Double.valueOf(latitude.getTextContent());

                    LatLng coord = new LatLng(lat,longi);
                    list.add(coord);
                    System.out.println(lat);
                    System.out.println(coord.latitude);
                }
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }
}
