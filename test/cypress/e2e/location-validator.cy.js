describe('Validate Location', () => {
	beforeEach(() => {
		cy.login(); 
		cy.language(); 
		cy.visit('/home'); 
	});

	it('Test insert location check-in', () => {
		cy.get(
			':nth-child(1) > :nth-child(6) > .-esm-appointments__appointments-actions__container____fi4O > .cds--btn'
		).click();
		cy.get(':nth-child(1) > .cds--radio-button__label > .cds--radio-button__appearance').click()
		cy.get('.-esm-patient-chart__visit-form__tablet___HslaG > .cds--btn--primary').click()
	});

	it('Test location check-in with empty address', () => {
		cy.get(
			':nth-child(7) > :nth-child(6) > .-esm-appointments__appointments-actions__container____fi4O > .cds--btn'
		).click();
		cy.get('.-esm-patient-chart__visit-form__tablet___HslaG > .cds--btn--primary').click()

	});

	it('Test location check-in with empty variation', () => {
		cy.get(
			':nth-child(7) > :nth-child(6) > .-esm-appointments__appointments-actions__container____fi4O > .cds--btn'
		).click();
		cy.get('.-esm-patient-chart__visit-form__tablet___HslaG > .cds--btn--primary').click()

	});
	
	it('Test location loop', () => {
		cy.get(
			':nth-child(7) > :nth-child(6) > .-esm-appointments__appointments-actions__container____fi4O > .cds--btn'
		).click();
		cy.get('.-esm-patient-chart__visit-form__tablet___HslaG > .cds--btn--primary').click()
		cy.get('.-esm-patient-chart__visit-form__tablet___HslaG > .cds--btn--primary').click()
		cy.get('.-esm-patient-chart__visit-form__tablet___HslaG > .cds--btn--primary').click()
	});
});
