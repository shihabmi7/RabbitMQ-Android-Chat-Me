package com.karthi.rabbitmq;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karthi.rabbitmq.MessageConsumer.OnReceiveMessageHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.UnsupportedEncodingException;

public class ReceiveMessageActivity extends Activity {
    private MessageConsumer mConsumer;
    private TextView mOutput;


    private String message = "";
    private String name = "";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        Toast.makeText(ReceiveMessageActivity.this, "RabbitMQ Chat Receive....!",
                Toast.LENGTH_LONG).show();


        final EditText etv = (EditText) findViewById(R.id.messagebox);
        etv.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                // If the event is a key-down event on the "enter" button
                if ((arg2.getAction() == KeyEvent.ACTION_DOWN)
                        && (arg1 == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    message = name + ": " + etv.getText().toString();
                    new send().execute(message);
                    etv.setText("");
                    return true;
                }
                return false;
            }
        });

        // The output TextView we'll use to display messages
        mOutput = (TextView) findViewById(R.id.output);

        // Create the consumer
        mConsumer = new MessageConsumer(ApplicationData.URL, ApplicationData.EXCHANGE_NAME, ApplicationData.EXCHANGE_TYPE);
        new Consumerconnect().execute();
        // register for messages
        mConsumer.setOnReceiveMessageHandler(new OnReceiveMessageHandler() {

            public void onReceiveMessage(byte[] message) {
                String text = "";
                try {
                    text = new String(message, "UTF8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                mOutput.append("\n" + text);
            }
        });


    }

    private class send extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {

                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("10.0.2.2");

                // my internet connection is a bit restrictive so I have use an
                // external server
                // which has RabbitMQ installed on it. So I use "setUsername"
                // and "setPassword"
                factory.setUsername("guest");
                factory.setPassword("guest");
                //factory.setVirtualHost("karthi");
                factory.setPort(5672);

                System.out.println("Host: " + factory.getHost() +
                        " Port: " + factory.getPort() + ""
                        + factory.getRequestedHeartbeat() + ""
                        + factory.getUsername());


                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.exchangeDeclare(ApplicationData.EXCHANGE_NAME, "fanout", true);
                channel.queueDeclare(ApplicationData.QUEUE_NAME, false, false, false, null);
                String tempstr = "";
                for (int i = 0; i < Message.length; i++)
                    tempstr += Message[i];

                channel.basicPublish(ApplicationData.EXCHANGE_NAME, ApplicationData.QUEUE_NAME, null,
                        tempstr.getBytes());

                channel.close();
                connection.close();

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }


    private class Consumerconnect extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {


                // Connect to broker
                mConsumer.connectToRabbitMQ();


            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        new Consumerconnect().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConsumer.dispose();
    }
}