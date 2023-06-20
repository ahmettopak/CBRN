package com.ahmet.cbrn_websocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class DeviceAPI {
    private static DeviceAPI instance;

    private List<UpdateListener> updateListeners;
    private List<InfoListener> infoListeners;
    private List<SensorListener> sensorListeners;
    private List<ShutdownListener> shutdownListeners;
    private List<EventListener> eventListeners;
    private List<GaslibMeasListener> gaslibMeasListeners;

    private List<Library> libraryList;
    private List<Library> trainingLibraryList;
    private List<Compound> compounds;
    private Measurement lastMeasurement;
    private String status;
    private boolean connected;
    private JSONObject info;
    private JSONArray gaslibMeasList;
    private boolean ready;

    private boolean poweredOff;
    private WebSocket ws;

    private DeviceAPI() {
        updateListeners = new ArrayList<>();
        infoListeners = new ArrayList<>();
        sensorListeners = new ArrayList<>();
        shutdownListeners = new ArrayList<>();
        eventListeners = new ArrayList<>();
        gaslibMeasListeners = new ArrayList<>();

        libraryList = new ArrayList<>();
        trainingLibraryList = new ArrayList<>();
        compounds = new ArrayList<>();
        lastMeasurement = null;
        status = null;
        connected = false;
        info = new JSONObject();
        gaslibMeasList = new JSONArray();
        ready = false;
        poweredOff = false;

        initSensorStream();
    }

    public static DeviceAPI getInstance() {
        if (instance == null) {
            instance = new DeviceAPI();
        }
        return instance;
    }

    public void addUpdateListener(UpdateListener listener) {
        updateListeners.add(listener);
    }

    public void addInfoListener(InfoListener listener) {
        infoListeners.add(listener);
    }

    public void addSensorListener(SensorListener listener) {
        sensorListeners.add(listener);
    }

    public void addShutdownListener(ShutdownListener listener) {
        shutdownListeners.add(listener);
    }

    public void addEventListener(EventListener listener) {
        eventListeners.add(listener);
    }

    public void addGaslibMeasListener(GaslibMeasListener listener) {
        gaslibMeasListeners.add(listener);
    }

    private void notifyUpdateListeners() {
        for (UpdateListener listener : updateListeners) {
            listener.onUpdate();
        }
    }

    private void notifyInfoListeners() {
        for (InfoListener listener : infoListeners) {
            listener.onInfo();
        }
    }

    private void notifySensorListeners(JSONObject data) {
        for (SensorListener listener : sensorListeners) {
            listener.onSensorData(data);
        }
    }

    private void notifyShutdownListeners(String reason) {
        for (ShutdownListener listener : shutdownListeners) {
            listener.onShutdown(reason);
        }
    }

    private void notifyEventListeners(JSONObject data) {
        for (EventListener listener : eventListeners) {
            listener.onEvent(data);
        }
    }

    private void notifyGaslibMeasListeners(JSONArray data) {
        for (GaslibMeasListener listener : gaslibMeasListeners) {
            listener.onGaslibMeas(data);
        }
    }

    private void handleSensorData(JSONObject data) {
        notifySensorListeners(data);
    }

    private void handleInfo(JSONObject data) {
        info = data;
        notifyInfoListeners();
    }

    private void handleMeasurement(JSONObject data) {
        try {
            lastMeasurement = new Measurement(
                    data.getDouble("value"),
                    data.getString("unit")
            );
            notifyUpdateListeners();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleStatus(String data) {
        status = data;
        notifyUpdateListeners();
    }

    private void handleShutdown(String data) {
        poweredOff = true;
        notifyShutdownListeners(data);
    }

    private void handleEvent(JSONObject data) {
        notifyEventListeners(data);
    }

    private void handleGaslibmeas(JSONArray data) {
        gaslibMeasList = data;
        notifyGaslibMeasListeners(data);
    }

    private void initSensorStream() {
        OkHttpClient client = new OkHttpClient();

        String wsUrl = "ws://10.42.0.1/api/device/stream";

        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);

                if (poweredOff) {
                    // must be after reboot
                    // Reload the page or perform necessary actions
                } else {
                    connected = true;
                    notifyUpdateListeners();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);

                try {
                    JSONObject msg = new JSONObject(text);
                    String type = msg.optString("type");
                    JSONObject content = msg.optJSONObject("content");

                    if (type != null && content != null) {
                        switch (type) {
                            case "sensors":
                                handleSensorData(content);
                                break;
                            case "meas":
                                handleMeasurement(content);
                                break;
                            case "status":
                                handleStatus(content.getString("status"));
                                break;
                            case "shutdown":
                                handleShutdown(content.getString("reason"));
                                break;
                            case "info":
                                handleInfo(content);
                                break;
                            case "event":
                                handleEvent(content);
                                break;
                            case "gaslibmeas":
                                handleGaslibmeas(content.getJSONArray("data"));
                                break;
                            default:
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);

                connected = false;
                notifyUpdateListeners();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);

                connected = false;
                notifyUpdateListeners();
            }
        };

        WebSocket webSocket = client.newWebSocket(request, webSocketListener);
    }

    public void setLibrary(int libId, int partId) {
        OkHttpClient client = new OkHttpClient();

        String url = "http://10.42.0.1/api/device/library/set?library_id=" + libId + "&part_id=" + partId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                // Handle response if needed
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Handle failure if needed
            }
        });
    }

    public Compound getCompound(String id) {
        for (Compound compound : compounds) {
            if (compound.getId().equals(id)) {
                return compound;
            }
        }
        return null;
    }

    public Library getLibrary(String id, boolean training) {
        List<Library> libList = training ? trainingLibraryList : libraryList;
        for (Library library : libList) {
            if (library.getId().equals(id)) {
                return library;
            }
        }
        return null;
    }

    public void doBlc() {
        OkHttpClient client = new OkHttpClient();

        String url = "http://10.42.0.1/api/device/blc";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                // Handle response if needed
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Handle failure if needed
            }
        });
    }

    public void init() {
        final int[] counter = {5};

        OkHttpClient client = new OkHttpClient();

        String measurementUrl = "http://10.42.0.1/api/device/measurement";
        Request measurementRequest = new Request.Builder()
                .url(measurementUrl)
                .build();

        client.newCall(measurementRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    lastMeasurement = new Measurement(
                            data.getDouble("value"),
                            data.getString("unit")
                    );
                    ready = --counter[0] <= 0;
                    notifyUpdateListeners();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Handle failure if needed
            }
        });

        String libraryUrl = "http://your-api-url/api/device/library";
        Request libraryRequest = new Request.Builder()
                .url(libraryUrl)
                .build();

        client.newCall(libraryRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    libraryList = parseLibraryList(data.getJSONArray("libraries"));
                    trainingLibraryList = parseLibraryList(data.getJSONArray("trainingLibraries"));
                    ready = --counter[0] <= 0;
                    notifyUpdateListeners();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Handle failure if needed
            }
        });

        String compoundsUrl = "http://10.42.0.1/api/device/compounds";
        Request compoundsRequest = new Request.Builder()
                .url(compoundsUrl)
                .build();

        client.newCall(compoundsRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try {
                    JSONArray data = new JSONArray(response.body().string());
                    compounds = parseCompoundList(data);
                    ready = --counter[0] <= 0;
                    notifyUpdateListeners();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Handle failure if needed
            }
        });

        String statusUrl = "http://10.42.0.1/api/device/status";
        Request statusRequest = new Request.Builder()
                .url(statusUrl)
                .build();

        client.newCall(statusRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    status = data.getString("status");
                    ready = --counter[0] <= 0;
                    notifyUpdateListeners();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Handle failure if needed
            }
        });

        String infoUrl = "http://10.42.0.1/api/device/info";
        Request infoRequest = new Request.Builder()
                .url(infoUrl)
                .build();

        client.newCall(infoRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    info = data;
                    ready = --counter[0] <= 0;
                    notifyInfoListeners();
                    notifyUpdateListeners();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Handle failure if needed
            }
        });
    }

    private List<Library> parseLibraryList(JSONArray jsonArray) throws JSONException {
        List<Library> libraryList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonLibrary = jsonArray.getJSONObject(i);
            String id = jsonLibrary.getString("id");
            String name = jsonLibrary.getString("name");
            libraryList.add(new Library(id, name));
        }
        return libraryList;
    }

    private List<Compound> parseCompoundList(JSONArray jsonArray) throws JSONException {
        List<Compound> compoundList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonCompound = jsonArray.getJSONObject(i);
            String id = jsonCompound.getString("id");
            String name = jsonCompound.getString("name");
            compoundList.add(new Compound(id, name));
        }
        return compoundList;
    }

    public interface UpdateListener {
        void onUpdate();
    }

    public interface InfoListener {
        void onInfo();
    }

    public interface SensorListener {
        void onSensorData(JSONObject data);
    }

    public interface ShutdownListener {
        void onShutdown(String reason);
    }

    public interface EventListener {
        void onEvent(JSONObject data);
    }

    public interface GaslibMeasListener {
        void onGaslibMeas(JSONArray data);
    }

    public class Measurement {
        private double value;
        private String unit;

        public Measurement(double value, String unit) {
            this.value = value;
            this.unit = unit;
        }

        public double getValue() {
            return value;
        }

        public String getUnit() {
            return unit;
        }
    }

    public class Library {
        private String id;
        private String name;

        public Library(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public class Compound {
        private String id;
        private String name;

        public Compound(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}