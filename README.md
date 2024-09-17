As a system administrator, I want to assign roles to users that are associated with multiple dealers, so that each user can have roles corresponding to different dealers within the system.

Acceptance Criteria:
Multiple Dealer Support: The system must allow the assignment of roles to a user with each role being associated with a different dealer, based on the data provided in the role DTOs.
Dealer Validation: Each role assigned must validate that the dealer associated with the role exists and is valid.
Error Handling: If the dealer provided for a role does not exist or is not associated with the customer's dealer, the system should return an appropriate error message.
Notification: Notifications should still be sent after role assignment using the appropriate dealer for each user role.
No Global Dealer Assumption: The system should no longer assume a single dealer for all roles in a batch assignment and must handle each role's dealer independently.
