@Test
	public void testScript2() throws Exception {
	    try {
	    DesiredCapabilities capabilities = LambdaTest.setUp();
	    String username = LambdaTest.username;
	    String accessKey = LambdaTest.accessKey;
	    RemoteWebDriver driver = new RemoteWebDriver(new URL("https://"+username+":"+accessKey+"@hub.lambdatest.com/wd/hub"),capabilities);		 driver.get("https://lambdatest.github.io/sample-todo-app/");		    driver.findElement(By.name("li2")).click();
	    driver.findElement(By.name("li3")).click();
	   driver.findElement(By.id("sampletodotext")).clear();	driver.findElement(By.id("sampletodotext")).sendKeys("Yes, Let's add it to list");				driver.findElement(By.id("addbutton")).click();
					driver.quit();					
		} catch (Exception e) {
				System.out.println(e.getMessage());
			    }
			}
	

	@Test
	public void testScript3() throws Exception {
		try {
	    DesiredCapabilities capabilities = LambdaTest.setUp();
	    String username = LambdaTest.username;
	    String accessKey = LambdaTest.accessKey;
	    RemoteWebDriver driver = new RemoteWebDriver(new URL("https://"+username+":"+accessKey+"@hub.lambdatest.com/wd/hub"),capabilities);			  driver.get("https://lambdatest.github.io/sample-todo-app/");		    driver.findElement(By.name("li4")).click();
	    driver.findElement(By.id("sampletodotext")).clear();	driver.findElement(By.id("sampletodotext")).sendKeys("Yes, Let's add  it!");
	  driver.findElement(By.id("addbutton")).click();
	  driver.quit();					
	} catch (Exception e) {
		System.out.println(e.getMessage());
			}
			}
