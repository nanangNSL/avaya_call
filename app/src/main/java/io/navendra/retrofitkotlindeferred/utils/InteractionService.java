package io.navendra.retrofitkotlindeferred.utils;

import android.app.Application;

import com.avaya.ocs.Config.ClientConfiguration;
import com.avaya.ocs.Config.WebGatewayConfiguration;
import com.avaya.ocs.OceanaCustomerWebVoiceVideo;
import com.avaya.ocs.Services.Device.Video.VideoDevice;
import com.avaya.ocs.Services.Work.Enums.PlatformType;
import com.avaya.ocs.Services.Work.Interactions.AudioInteraction;
import com.avaya.ocs.Services.Work.Interactions.Listeners.OnAudioDeviceChangeListener;
import com.avaya.ocs.Services.Work.Interactions.VideoInteraction;
import com.avaya.ocs.Services.Work.Schema.Resource;
import com.avaya.ocs.Services.Work.Schema.Service;
import com.avaya.ocs.Services.Work.Work;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class InteractionService {

    private static final String TAG = InteractionService.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private Application application;

    private AudioInteraction audioInteraction;
    private VideoInteraction videoInteraction;

    private String oceanaDestinationAddress;
    private String numbertodial="";
    private String uui="";
    private String contextId;
//    private AvayaPlatformPreferences oceanaPreferences;

    private static InteractionService instance = new InteractionService();

    public static InteractionService getInstance() {
        return instance;
    }

    private InteractionService() {
    }

    public void init(Application application) {
        this.application = application;
    }

    private Work createWork() {
        mLogger.d("entering createWork");

        Work work;
//        oceanaPreferences = settingsService.retrievePreferences();
//        WebGatewayPreferences gatewayPreferences = settingsService.retrieveWebGatewayPreferences();

//        if (oceanaPreferences.isAvailable()) {
            oceanaDestinationAddress = Constants.DEST_NUMBER;
//            contextId = oceanaPreferences.getContext();
            // 1: CREATE CONFIG
//            Config config = new Config(oceanaPreferences.getAmcServer());
//            config.setSecure(true);
//            config.setPort(oceanaPreferences.getAmcPort());
//
//            if (oceanaPreferences.getAmcUrlPath() != null && !oceanaPreferences.getAmcUrlPath().isEmpty()) {
//                config.setUrlPath(oceanaPreferences.getAmcUrlPath());
//            }

            WebGatewayConfiguration webGatewayConfiguration = new WebGatewayConfiguration();
            webGatewayConfiguration.setWebGatewayAddress(Constants.SERVER_PLACEHOLDER);
            webGatewayConfiguration.setPort(Integer.parseInt(Constants.PORT_PLACEHOLDER));
            webGatewayConfiguration.setSecure(true);
            webGatewayConfiguration.setWebGatewayUrlPath(Constants.AAWG_TOKEN_URL_PATH_PLACEHOLDER2);

            ClientConfiguration clientConfiguration = new ClientConfiguration();
//            clientConfiguration.setConfig(config);
            clientConfiguration.setWebGatewayConfiguration(webGatewayConfiguration);
            // 2: CREATE OCEANA CLIENT
            OceanaCustomerWebVoiceVideo client = new OceanaCustomerWebVoiceVideo(clientConfiguration);

            client.registerLogger(Level.ALL);
            mLogger.d("SDK version number: " + client.getVersionNumber());

            // 3: CREATE WORK
            work = client.createWork();
            work.setContext(uui);

            // 4: ENHANCED WORK API
//            if (oceanaPreferences.getLocale() != null && !oceanaPreferences.getLocale().isEmpty()) {
//                work.setLocale(oceanaPreferences.getLocale());
//            }
//
//            if (oceanaPreferences.getTopic() != null && !oceanaPreferences.getTopic().isEmpty()) {
//                work.setTopic(oceanaPreferences.getTopic());
//            }
//
//            if (oceanaPreferences.getStrategy() != null && !oceanaPreferences.getStrategy().isEmpty()) {
//                work.setRoutingStrategy(oceanaPreferences.getStrategy());
//            }

            List<Resource> resource = createResources();
            work.setResources(resource);

            List<Service> services = createServices();
            work.setServices(services);

//            work.setPlatformType(settingsService.getType());
//
//            work.setPlatformContext(oceanaPreferences.getContext());
            /*
             * Alternate method of creating routing attributes.
             * The below mechanism creates a simple work request with the attributes provided
             */
            // List<Attribute> attributes = settingsService.retrieveOceanaRoutingAttributes();
            // work.setAttributes(attributes);
//        } else {
//            String reason = "Invalid or missing setting";
//            throw new IllegalArgumentException("Error creating work - " + reason);
//        }
        return work;
    }

    private List<Resource> createResources() {
        List<Resource> resources = new ArrayList<>();

//        if (!oceanaPreferences.getResourceId().isEmpty() && !oceanaPreferences.getSourceName().isEmpty()) {
//            Resource resource = new Resource();
//            resource.setNativeResourceId(oceanaPreferences.getResourceId());
//            resource.setSourceName(oceanaPreferences.getSourceName());
//            resources.add(resource);
//        }

        return resources;
    }

    private List<Service> createServices() {
        List<Service> services = new ArrayList<>();

//        Attributes attributes = settingsService.retrieveServiceMapAttributes();
//
//        if (attributes != null && !attributes.getMap().isEmpty()) {
            Service service = new Service();

//            service.setAttributes(attributes);
//            if (oceanaPreferences.getPriority() != null || !oceanaPreferences.getPriority().isEmpty()) {
                service.setPriority(5);
//            } else {
//                service.setPriority(Integer.parseInt(oceanaPreferences.getPriority()));
//            }
            services.add(service);
//        }

        return services;
    }

    public AudioInteraction createAudioInteraction(OnAudioDeviceChangeListener listener,String callee,String uui2) {
        mLogger.d("entering createAudioInteraction");
        numbertodial=callee;
        uui=uui2;
        audioInteraction = createWork().createAudioInteraction(application, listener);
        //If you don't want to handle audio device's changes use
        //createWork().createAudioInteraction(application);
        //If you want to override audio device's switch logic to you custom
        //createWork().createAudioInteraction(application, listener, new MyCustomAudioDeviceSwitchHelper());
        //If you want to disable auto device switching inside SDK at all
        //and handle it in onAudioDeviceListChanged() callback use:
//        audioInteraction.setHandleAudioDeviceSwitch(false);

        if (numbertodial != null || !numbertodial.isEmpty()) {
            audioInteraction.setDestinationAddress(numbertodial);
        }
        if (uui != null || !uui.isEmpty()) {
            audioInteraction.setContext(uui);
        }
        //Set the platform as Oceana or Elite
        audioInteraction.setPlatformType(PlatformType.ELITE);
        return audioInteraction;
    }

    public VideoInteraction createVideoInteraction(OnAudioDeviceChangeListener listener) {
        mLogger.d("entering createVideoInteraction");

        videoInteraction = createWork().createVideoInteraction(application, listener);
        //If you don't want to handle audio device's changes use
        //createWork().createVideoInteraction(application);
        //If you want to override audio device's switch logic to you custom
        //createWork().createVideoInteraction(application, listener, new MyCustomAudioDeviceSwitchHelper());
        //If you want to disable auto device switching inside SDK at all
        //and handle it in onAudioDeviceListChanged() callback use:
        //audioInteraction.setHandleAudioDeviceSwitch(false);

        if (oceanaDestinationAddress != null || !oceanaDestinationAddress.isEmpty()) {
            videoInteraction.setDestinationAddress(oceanaDestinationAddress);
        }
        if (contextId != null || !contextId.isEmpty()) {
            videoInteraction.setContext(contextId);
        }
        //Set the platform as Oceana or Elite
        videoInteraction.setPlatformType(PlatformType.ELITE);
        return videoInteraction;
    }

    public AudioInteraction getAudioInteraction() throws InteractionNotInitializedException {
        if (audioInteraction == null) {
            throw new InteractionNotInitializedException("Audio Interaction has not been created. Ensure createAudioInteraction has been invoked!");
        }
        return audioInteraction;
    }

    public VideoInteraction getVideoInteraction() throws InteractionNotInitializedException {
        if (videoInteraction == null) {
            throw new InteractionNotInitializedException("Video Interaction has not been created. Ensure createVideoInteraction has been invoked!");
        }
        return videoInteraction;
    }

    public VideoDevice setupVideoDevice() throws InteractionNotInitializedException {
        mLogger.d("entering setupVideoDevice");
        if (videoInteraction == null) {
            throw new InteractionNotInitializedException("Video Interaction has not been created, cannot create VideoDevice. Ensure createVideoInteraction has been invoked!");
        }

        VideoDevice videoDevice = null;
        try {
            videoDevice = videoInteraction.getVideoDevice();
        } catch (Exception e) {
            mLogger.e("Error in setupVideoDevice with message: " + e.getMessage(), e);
        }
        return videoDevice;
    }
}
