/**
 * Copyright...
 */
package org.openmrs.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class })
public class LocationUtilityTest {

    private LocationService locationService;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Context.class);
        locationService = mock(LocationService.class);
        LocationUtility.setDefaultLocation(null);
    }

    @Test
    public void getDefaultLocation_shouldRefreshCacheWhenSessionIsOpen() {
        when(Context.isSessionOpen()).thenReturn(true);
        when(Context.getLocationService()).thenReturn(locationService);

        Location updated = new Location(2);
        when(locationService.getDefaultLocation()).thenReturn(updated);

        Location result = LocationUtility.getDefaultLocation();

        assertEquals(updated, result);
        verify(locationService, times(1)).getDefaultLocation();
    }

    @Test
    public void getDefaultLocation_shouldNotRefreshCacheWhenSessionIsClosed() {
        Location initial = new Location(1);
        LocationUtility.setDefaultLocation(initial);

        when(Context.isSessionOpen()).thenReturn(false);
        when(Context.getLocationService()).thenReturn(locationService);

        Location updated = new Location(2);
        when(locationService.getDefaultLocation()).thenReturn(updated);

        Location result = LocationUtility.getDefaultLocation();

        assertEquals(initial, result);
        verify(locationService, never()).getDefaultLocation();
    }
}
