| Key | Value                                    |
| --- |------------------------------------------|
| Date: | 19/03/2024                               |
| Time: | 13:45                                    |
| Location: | Flux Hall B                              |
| Chair | Kajus Kuncaitis                          |
| Minute Taker | Thomas                                   |
| Attendees: | Kajus, Thomas, Liam, Güney, Bogdan, Adam |
### Agenda Items:
- ##### Intro (3 min)
  - Opening by chair (1 min)
  - Check in: a round of how is everyone doing (1 min)
  - Approval of the agenda - Does anyone have any additions? (1 min)
- ##### Main Matter (34 min)
  - **Last Week's Progress** (18 min)
    - A round of showing the team the progress we've made last week. (7 min discussion)
    - Ensure the related issues are closed/resolved on GitLab (3 min)
    - Discuss the feedback for Tasks and planning & Buddycheck (10 min) (check the feedback before the meeting)
  - **What's Next?** (22 min)
    - Brainstorm and decide what to work on this week. (10 min)
      - Start working on app design mock-ups?
      - HCI / Accessibility of the app (read the product assignment HCI/Accessibility document on Brightspace)
    - Create issues for the features (5 min)
    - Assign the issues to people (3 min)
- ##### Wrap up (7 min)
  - Announcements by the TA (2 min)
  - Decide on the next chair and minute taker. (1 min)
  - Feedback: What went well and what could be improved? (2 min)
  - If anyone has anything to add to the meeting, do it now. (1 min)
  - Closure (1 min)

Total planned meeting time: 45 min + 5 min buffer

# Notes:


## Last week's progress:


### Discussing feedback

## Code contributions and reviews
Strong points:
  - in general, we've done well regarding the code contributions, but there are still some key points to look at

Key points to improve upon:
  - give commits a more detailed/descriptive message instead of 'bug fix' or 'minor changes' these are not informative about the content of it
  - don't wait till sunday to merge everything, could help to merge/make MR more often and earlier to get feedback
  - make sure to mark threads as resolved before merging
  - make sure that all pipelines succeed, some were failing

## Tasks and planning
Key points to improve upon:
  - user stories should be converted into smaller actionable tasks
  - the tasks themselves should be directly linked to user stories
  - some issues still have no assigned labels
  - tasks sizes are ok, but we should be able to do multiple tasks a week (each)
  - some tasks have no description and no acceptance criteria
  - almost no tasks have time estimations linked to them
  - most tasks are assigned to people, but almost none of them contain the time spent on it
  - we should take time estimations into account when assigning tasks for the week
  - we haven't really done so, but if multiple people collaborate on the same branch, a nested branch should be used
  - until this point we haven't had any big role rotations, which is a requirement

## Technology
Strong points:
  - our javaFX controllers use dependency injection like we were expected to, that's great!
  - Data transfer is good, nothing to improve necessarily

Key points to improve upon:
  - we don't know yet what external types are, but they are important
  - we miss services and regular controllers (for WS endpoints)
  - at the time of reviewing, we didn't have any functionality relying on websockets
  - we need to implement long polling

## Goals for the week

### Striving points
- we want to complete basic requirements as fast as possible

## Issues for the week
- implement long polling
- fixing websocket issue
- implement display for expenses on event page
- implement order by feature to order events (admin)
- connect event and expense page to websockets
- mock-up creation: 
  - tip use realtime colors and implement accessibility features (use the backlog make sure we meet the requirements)

# Next weeks meeting
  - chair:
  Liam
  - minute taker:
  Güney
  
