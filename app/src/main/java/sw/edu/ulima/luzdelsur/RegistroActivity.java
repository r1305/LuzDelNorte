package sw.edu.ulima.luzdelsur;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Date;

public class RegistroActivity extends AppCompatActivity {

    EditText fecha;
    EditText sum;
    EditText consumo;
    FloatingActionButton fab;
    FloatingActionButton guardar;
    String f;

    private final String NAMESPACE = "http://arqui.ws/";
    private String URL;
    private final String SOAP_ACTION = "http://172.16.245.2:8080/WS/";
    String METHOD_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        guardar=(FloatingActionButton) findViewById(R.id.save);

        sum=(EditText)findViewById(R.id.numeroSum);
        fecha=(EditText)findViewById(R.id.fecha);
        consumo=(EditText)findViewById(R.id.consumo);

        consumo.setEnabled(false);

        Date d=new Date();
        d.getTime();
        Time today=new Time(Time.getCurrentTimezone());
        today.setToNow();
        fecha.setText(today.format("%d/%m/%Y"));
        Toast.makeText(RegistroActivity.this, "fecha: " + fecha.getText(), Toast.LENGTH_LONG).show();
        f=fecha.getText().toString();
        fab= (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                URL= "http://172.16.245.2:8080/WS/Validar?WSDL";
                METHOD_NAME="validar";
                validar(sum.getText().toString());



            }
        });
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URL= "http://172.16.245.2:8080/WS/Server?WSDL";
                METHOD_NAME="ponerencola";
                encolar(sum.getText().toString(),f,consumo.getText().toString());
                Toast.makeText(RegistroActivity.this,
                        "fecha: " +f
                        +",consumo: " +consumo.getText()
                        +", sum: "+sum.getText().toString(),
                        Toast.LENGTH_LONG).show();

            }
        });
    }

    public void validar(final String num){

        Thread networkThread = new Thread() {

            @Override public void run() {

                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                request.addProperty("suministro",num);


                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);

                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

                try {
                    androidHttpTransport.call(SOAP_ACTION, envelope); SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
                    final String str = response.toString();
                    runOnUiThread (new Runnable(){ public void run() {

                        if(str.equals("ok")){
                            Toast.makeText(RegistroActivity.this,"Numero de suministro valido",Toast.LENGTH_LONG).show();


                            consumo.setEnabled(true);
                            sum.setEnabled(false);
                            fab.setVisibility(View.GONE);
                            guardar.setVisibility(View.VISIBLE);

                        }else{
                            Toast.makeText(RegistroActivity.this,"Numero de suministro incorrecto",Toast.LENGTH_SHORT).show();
                        }
                    }});
                }catch (Exception e) {e.printStackTrace();}
            }};
        networkThread.start();
    }

    public void encolar(final String num, final String f, final String c){

        Thread networkThread = new Thread() {

            @Override public void run() {

                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                request.addProperty("suministro",num);
                request.addProperty("fecha",f);
                request.addProperty("consumo",c);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);

                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

                try {
                    androidHttpTransport.call(SOAP_ACTION, envelope); SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
                    final String str = response.toString();
                    runOnUiThread (new Runnable(){ public void run() {

                        if(str.equals("ok")){
                            Toast.makeText(RegistroActivity.this,"Registro en cola",Toast.LENGTH_LONG).show();

                            consumo.setEnabled(false);
                            consumo.setText("");
                            sum.setText("");
                            sum.setEnabled(true);
                            fab.setVisibility(View.VISIBLE);
                            guardar.setVisibility(View.GONE);

                        }
                    }});
                }catch (Exception e) {e.printStackTrace();}
            }};
        networkThread.start();
    }
}
