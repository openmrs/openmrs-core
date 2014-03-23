# Contributing to OpenMRS

## Reporting Bugs

1. Please check to see if you are running the latest version of OpenMRS; the bug may already be resolved. If you are running an old version, it may no longer be supported. Check our [Unsupported Releases](https://wiki.openmrs.org/x/2RAz) page for details.

2. Search for similar problems using the [OpenMRS Global Search](http://search.openmrs.org); it may already be an identified problem.

3. Make sure you can reproduce your problem on our demo site at [demo.openmrs.org](http://demo.openmrs.org).

4. If you know the problem is related to the function or performance of a specific add-on module you're running, please consult the [Module Repository](http://modules.openmrs.org) and that module's documentation about how to get support.

5. If you believe the problem is with OpenMRS itself, or if you're honestly not sure, you can [file the bug report in the OpenMRS Trunk project in JIRA](https://tickets.openmrs.org/secure/CreateIssue.jspa?pid=10000&issuetype=1&Create=Create). You'll need to have an [OpenMRS ID](http://id.openmrs.org) to do so. You'll receive e-mail updates as the bug is investigated and resolved.

### Bug report contents

Regardless of whether you're reporting the bug to the OpenMRS core team or to a module developer, to help those developers understand the problem, please include as much information as possible:

1. A clear description of how to recreate the error, including any error messages shown.
2. The version of OpenMRS you are using.
3. The type and version of the database you are using, if known.
4. If you are using any additional modules, custom templates, stylesheets, or messages, please list them.
5. If applicable, please copy and paste the full Java stack trace.
6. If you have already communicated with a developer about this issue, please provide their name.


## Requesting New Features

1. We encourage you to discuss your feature ideas with our community before creating a New Feature issue in our issue tracker. Use our [global OpenMRS search](http://search.openmrs.org) to see if someone has already proposed similar features. If not, use our [mailing lists](http://go.openmrs.org/lists) or [OpenMRS Talk](http://talk.openmrs.org) to discuss your ideas with us.

2. Provide a clear and detailed explanation of the feature you want and why it's important to add. The feature must apply to a wide array of users of OpenMRS; for smaller, more targeted "one-off" features, you might consider writing an add-on module for OpenMRS. You may also want to provide us with some advance documentation on the feature, which will help the community to better understand where it will fit.

3. If you're an advanced programmer, build the feature yourself (refer to the "Contributing (Step-by-step)" section below).

## Contributing (Step-by-step)

1. After finding a JIRA issue for the "Ready for Work" bug or feature on which you'd like to work, claim the issue by clicking the "Claim Issue" button. This will assign the issue to you and change its status to "In Progress".

2. Clone the Repo:

        git clone git://github.com/openmrs/openmrs-core.git

3. Create a new Branch:

        cd openmrs-core
        git checkout -b new_openmrs-core_branch

 > Please keep your code clean: one feature or bug-fix per branch. If you find another bug, you want to fix while being in a new branch, please fix it in a separated branch instead.

4. Code
  * Adhere to common conventions you see in the existing code
  * Include tests, and ensure they pass
  * Search to see if your new functionality has been discussed using our [OpenMRS global search](http://search.openmrs.org), and include updates as appropriate

4. Follow the Coding Conventions described at: [https://wiki.openmrs.org/x/MxEz](https://wiki.openmrs.org/x/MxEz).

  > However, please note that **pull requests consisting entirely of style changes are not welcome on this project**. Style changes in the context of pull requests that also refactor code, fix bugs, improve functionality *are* welcome.

5. Commit

  For every commit please write a short (max 72 characters) summary in the first line followed with a blank line and then more detailed descriptions of the change. Use markdown syntax for simple styling. Please include any JIRA issue numbers in your summary.

  **NEVER leave the commit message blank!** Provide a detailed, clear, and complete description of your commit!


6. Update your branch

  ```
  git fetch origin
  git rebase origin/master
  ```

7. Fork

  ```
  git remote add mine git@github.com:<your user name>/openmrs-core.git
  ```

8. Push to your remote

  ```
  git push mine new_openmrs-core_branch
  ```

9. Issue a Pull Request

  Before submitting a pull-request, clean up the history, go over your commits and squash together minor changes and fixes into the corresponding commits. You can squash commits with the interactive rebase command:

  ```
  git fetch origin
  git checkout new_openmrs-core_branch
  git rebase origin/master
  git rebase -i

  < the editor opens and allows you to change the commit history >
  < follow the instructions on the bottom of the editor >

  git push -f mine new_openmrs-core_branch
  ```
  In order to make a pull request,
  * Navigate to the OpenMRS repository you just pushed to (e.g. https://github.com/your-user-name/openmrs-core)
  * Click "Pull Request".
  * Write your branch name in the branch field (this is filled with "master" by default)
  * Click "Update Commit Range".
  * Ensure the changesets you introduced are included in the "Commits" tab.
  * Ensure that the "Files Changed" incorporate all of your changes.
  * Fill in some details about your potential patch including a meaningful title.
  * Click "Send pull request".
  * In JIRA, open your issue and click the "Request Code Review" button to change the issue's status to "Code Review".

  Thanks for that -- we'll get to your pull request ASAP, we love pull requests!

10. Responding to Feedback

  The OpenMRS team may recommend adjustments to your code. Part of interacting with a healthy open source community requires you to be open to learning new techniques and strategies; *don't get discouraged!* Remember: if the OpenMRS team suggests changes to your code, **they care enough about your work that they want to include it**, and hope that you can assist by implementing those revisions on your own.

  > Though we ask you to clean your history and squash commit before submitting a pull-request, please do not change any commits you've submitted already (as other work might be build on top).
