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
        Location updated = new Location(2);
        when(Context.getLocationService()).thenReturn(locationService);
        when(locationService.getDefaultLocation()).thenReturn(updated);
        assertEquals(updated, LocationUtility.getDefaultLocation());
    }

    @Test
    public void getDefaultLocation_shouldNotRefreshCacheWhenSessionIsClosed() {
        Location initial = new Location(1);
        LocationUtility.setDefaultLocation(initial);
        when(Context.isSessionOpen()).thenReturn(false);

        Location updated = new Location(2);
        when(Context.getLocationService()).thenReturn(locationService);
        when(locationService.getDefaultLocation()).thenReturn(updated);

        assertEquals(initial, LocationUtility.getDefaultLocation());
    }
}
