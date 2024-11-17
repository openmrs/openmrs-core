// -- Login with user and password
Cypress.Commands.add('login', (
  user = Cypress.env('user'), 
  password = Cypress.env('password')
) => {

  cy.visit('/login')

  cy.wait(1000)
  cy.get('#username').clear()
  cy.get('#username').type(user)
  cy.get('button:contains("Continue")').click()

  cy.wait(1000)
  cy.get('#password').clear()
  cy.get('#password').type(password)
  cy.get('button:contains("Log in")').click()

})