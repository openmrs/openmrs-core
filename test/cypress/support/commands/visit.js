Cypress.Commands.overwrite('visit', (originalFn, url) => {
  originalFn(url)
  return cy.get('body').then(() => {
    cy.wait(1000)
  })
})