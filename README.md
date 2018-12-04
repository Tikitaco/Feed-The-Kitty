This is the final project for CMSC436 Programming Handheld Systems at UMD. 
We were originally given a project titled "Feed The Kitty" and a short description of an application that would allow users to create events and split payments. Eventually this idea became "Fat Cat", an app that allows users to add friends, add bank accounts, view fund transfers, create events, and split costs. 

This app is written in Java for the Android platform by Andrew J, Brianne N, Eric G, Jacob E, Jasper L, and Stephen H.

**Setting up payments with Dwolla sandbox:**
1. After creating an account, tap Funding Sources in the side menu or Payments to begin
2. Fill out the information using fake data, tap Enable Payments, make sure that the toast says "Payment account created successfully"
3. Restart, tap Funding Sources in the side menu
4. Tap the floating plus button in the bottom right
5. Choose any nickname, the routing number must be 222222226 for Dwolla's sandbox testing, choose any random number that has between 4-16 digits, choose either checking or savings, then tap Add Fund and wait
6. Wait until the Verify Micro Deposits dialog pops up, select any value between 0.01-0.08 for both micro deposits, do not add a dollar sign, tap Verify, make sure the toast says "Fund source successfully added and verified"
7. Restart, you should be able to select a fund to use when paying for an item

**Verifying on Dwolla dashboard:**
Contact us for Dwolla sandbox credentials
