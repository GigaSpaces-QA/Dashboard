package com.gigaspaces.quality.dashboard.client.icons;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Icons extends ClientBundle{

    @Source( "thumbs_up.png" )
    ImageResource thumbUp();
    
    @Source( "thumbs_down.png" )
    ImageResource thumbDown();
    
    @Source( "warning.png" )
    ImageResource warning();

    @Source( "alarm.png" )
    ImageResource alarm();
}