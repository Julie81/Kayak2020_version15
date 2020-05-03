package fr.gleizes.kayak2020_version15;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;

import android.location.Location;
import android.location.LocationListener;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//     https://console.developers.google.com/flows/enableapi?apiid=maps_android_backend&keyType=CLIENT_SIDE_ANDROID&r=B3:17:02:47:C5:9E:C6:E4:7A:53:60:6F:03:3B:E4:14:90:03:65:43\%3Bcom.example.Kayak_2020

public class ConfigurationTrajet extends AppCompatActivity implements OnMapReadyCallback {
    //initialisation des variables
    Button button, buttonlink, buttonstart;
    GoogleMap gMap;
    Polyline path;
    List list = new ArrayList();
    int j;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_trajectoire);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); //mettre en mode satelite
        gMap.setMyLocationEnabled(true); // afficher sa localisation
        button = findViewById(R.id.button);  //button clear
        buttonlink=findViewById(R.id.buttonrelier);
        buttonstart=findViewById(R.id.buttonstart);

        //list = list;
        j = 0;
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //creat marker
                MarkerOptions markerOptions = new MarkerOptions();
                //set marker pos
                markerOptions.position(latLng);
                //set lat et long on map
                markerOptions.title(String.valueOf(j));
                j = j+1;
                list.add(latLng);
                //list.add(latLng.longitude);
                //ad marker on map
                gMap.addMarker(markerOptions);
            }
        });

        button.setOnClickListener(new View.OnClickListener() { //nettoyer clear la map pour les marqueurs
            @Override
            public void onClick(View view) {
                gMap.clear(); // on nettoie la map
                list.clear(); // on vide la list
                j=0;
            }
        });
        buttonlink.setOnClickListener(new View.OnClickListener() { //configuration du bouton link
            @Override
            public void onClick(View view) {
                PolylineOptions polylineOptions = new PolylineOptions().addAll(list).clickable(true); //nous ajoutons toute la liste
                path = gMap.addPolyline(polylineOptions);

                path.setColor(Color.parseColor("#00ff00")); //couleur verte
                path.setWidth(10); // taille du traie


            }

        });

        buttonstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openactivity();

            }
        });


    }
    public void openactivity(){
        Intent intent = new Intent(this, navigationGuiding.class);
        intent.putExtra("teste",34);
        intent.putExtra("lalist", (Serializable) list);
        //intent.putExtra("lalist", (Parcelable) list);
        XMLCreation();
        startActivity(intent);

    }

    protected void XMLCreation () {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //création du parseur XML
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            //création d'un document vierge
            final Document document = builder.newDocument();

            //on crée la racine du XML
            final Element racine = document.createElement("repertoire");
            document.appendChild(racine);

            //création du commentaire du nom du trajet
            final Comment commentaire = document.createComment("Trajet 1");
            racine.appendChild(commentaire);

            //on parcourt la liste des longitudes et latitudes des différents marqueurs
            System.out.println(list.size());
            for (int i = 0; i < list.size(); i++) {
                //System.out.println(((LatLng)list.get(i)).latitude);
                //System.out.println(((LatLng)list.get(i)).longitude);

                //rajout d'une balise de position
                final Element balise = document.createElement("balise");
                racine.appendChild(balise);
                String baliseid = String.valueOf(i);
                balise.setAttribute("id", baliseid);

                //rajout de la longitude et de la latitude
                final Element longitude = document.createElement("longitude");
                final Element latitude = document.createElement("latitude");

                balise.appendChild(longitude);
                balise.appendChild(latitude);

                String longi = String.valueOf(((LatLng) list.get(i)).longitude);
                String lat = String.valueOf(((LatLng) list.get(i)).latitude);

                longitude.appendChild(document.createTextNode(longi));
                latitude.appendChild(document.createTextNode(lat));
            }

            //on affiche le document XML
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(document);

            //on souhaite l'afficher dans un fichier
            final StreamResult sortie = new StreamResult(Environment.getExternalStorageDirectory() + "/Android/data/"
                    + getPackageName() + "/balise1.xml");
            //final StreamResult sortie = new StreamResult("/Download/balise1.xml");
            //System.out.println(Environment.getDownloadCacheDirectory()+"/file.xml");
            //System.out.println(source.toString());
            //System.out.println(Environment.getExternalStorageState());

            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                System.out.println("OK");
            } else {
                System.out.println("Problème carte SD");
            }

            //on écrit l'en-tête du fichier XML
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}intent-amount", "2");
            transformer.transform(source, sortie);


        } catch (final ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
