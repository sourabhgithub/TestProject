Fetch dealerId from each UserRoleDto: We now get the dealerId from the roleDto object for each role in the loop.
Move dealer fetching logic inside the loop: Instead of assigning a global dealerId, we fetch the Dealer for each role inside the loop based on the dealerId from the corresponding UserRoleDto.
Minimal structural change: Only modified the loop where roles are inserted, keeping the rest of the logic unchanged.
