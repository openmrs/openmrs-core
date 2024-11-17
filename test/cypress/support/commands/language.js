Cypress.Commands.add('language', (
  language = Cypress.env('language')
) => {
  cy.get('[data-testid="searchPatientIcon"]', { timeout: 15000 })
    .should('be.visible')

  cy.wait(1000)
  cy.get(':nth-child(3) > .cds--tooltip-trigger__wrapper > .cds--btn--icon-only').click({ force: true })
  cy.get('[data-extension-id="change-language"] > .cds--switcher__item > .cds--switcher__item-link > .cds--btn').click({ force: true })
  cy.contains(language).click({ force: true })
  cy.get('.-esm-primary-navigation__change-language__submitButton___a1yfJ').then(($btn) => {
    if ($btn.is(':enabled')) {
      cy.wrap($btn).click({ force: true, multiple: true })
    }
  })
})