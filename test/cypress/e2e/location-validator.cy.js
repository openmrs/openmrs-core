describe('Validate Location', () => {
	beforeEach(() => {
		cy.login(); 
		cy.language(); 
		cy.visit('/home'); 
	});

	it('Test insert location check-in', () => {
		// Clica no botão de check-in
		cy.get(
			':nth-child(1) > :nth-child(6) > .-esm-appointments__appointments-actions__container____fi4O > .cds--btn'
		).click();

		// Clica no ícone de excluir o endereço
		cy.get('.cds--header > .cds--header__action > .omrs-icon').click();

		// Clica no ícone de lista do endereço
		cy.get('.cds--list-box__menu-icon').click();

		// Seleciona o primeiro endereço da lista
		cy.get(':nth-child(1) > .cds--radio-button__label > .cds--radio-button__appearance').click();

		// Clica no botão de salvar
		cy.get('.-esm-patient-chart__visit-form__tablet___HslaG > .cds--btn--primary').click();

		// Verifica se o check-in foi realizado com sucesso
		cy.get('.notification-success').should('contain', 'Check-in successful'); // Substitua pelo seletor e mensagem corretos
	});

	it('Test location check-in with empty address', () => {
		// Clica no botão de check-in
		cy.get(
			':nth-child(7) > :nth-child(6) > .-esm-appointments__appointments-actions__container____fi4O > .cds--btn'
		).click();

		// Clica no ícone de excluir o endereço
		cy.get('.cds--header > .cds--header__action > .omrs-icon').click();

		// Tenta salvar sem selecionar um endereço
		cy.get('.-esm-patient-chart__visit-form__tablet___HslaG > .cds--btn--primary').click();

		// Verifica se a mensagem de erro é exibida
		cy.get('.notification-error').should('contain', 'Address is required'); // Substitua pelo seletor e mensagem corretos
	});
});
