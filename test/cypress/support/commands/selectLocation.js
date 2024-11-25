Cypress.Commands.add('selectLocation', (
  location = Cypress.env('location')
) => {
  cy.get('body').then(($body) => {
    if ($body.find('.-esm-login__location-picker__locationPickerContainer___t4nEl').length > 0) {
      cy.get(`.cds--radio-button__label:contains(${location})`).click();
      cy.get('.cds--btn').click();
    }
  });
})