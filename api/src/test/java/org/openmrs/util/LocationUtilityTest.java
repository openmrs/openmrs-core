package org.openmrs.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;

import static org.junit.Assert.*;
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
        // Mock static Context class
        PowerMockito.mockStatic(Context.class);

        // Create mock LocationService
        locationService = mock(LocationService.class);

        // Reset cache before each test
        LocationUtility.setDefaultLocation(null);
    }
    
    @Test
    public void getDefaultLocation_shouldRefreshCacheWhenSessionIsOpen() {
        // Mock Context.isSessionOpen()
        when(Context.isSessionOpen()).thenReturn(true);

        // Mock new default location
        Location updated = new Location(2);
        when(Context.getLocationService()).thenReturn(locationService);
        when(locationService.getDefaultLocation()).thenReturn(updated);

        // Call method
        Location result = LocationUtility.getDefaultLocation();

        assertEquals(updated, result);
    }

    @Test
    public void getDefaultLocation_shouldNotRefreshCacheWhenSessionIsClosed() {
        // Set initial cached value
        Location initial = new Location(1);
        LocationUtility.setDefaultLocation(initial);

        // Mock session closed
        when(Context.isSessionOpen()).thenReturn(false);

        // Even if service returns new value, cache should NOT refresh
        Location updated = new Location(2);
        when(Context.getLocationService()).thenReturn(locationService);
        when(locationService.getDefaultLocation()).thenReturn(updated);

        Location result = LocationUtility.getDefaultLocation();

        assertEquals(initial, result);  // must still be the old one
    }
}
