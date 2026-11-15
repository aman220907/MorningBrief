# Morning Brief — setup guide (no coding needed)

An Android app that:
1. Rings every day at the time you pick, like a normal alarm.
2. When you tap **Dismiss**, waits 10 minutes, then shows a notification listing your
   **unread emails that Gmail marked "Important"** (from the last 2 days).
   Tapping that notification opens the Gmail app.

The app only *reads* emails. It never sends, deletes or marks anything as read.

---

## Part A — Get a Gmail "app password" (5 min, on a computer or phone)

The app logs in to Gmail with a special 16-letter password instead of your real one.

1. Go to https://myaccount.google.com/security and turn on **2-Step Verification** (if it is off).
2. Go to https://myaccount.google.com/apppasswords, name it "Morning Brief", and click **Create**.
3. Copy the 16 letters Google shows (spaces don't matter). You'll paste them into the app later.
4. In Gmail on a **computer**: Settings (gear) > **See all settings** > **Labels** tab.
   Find **Important** and make sure **Show in IMAP** is ticked.
   Also check the **Forwarding and POP/IMAP** tab: if there is an IMAP setting, make sure IMAP is enabled.

If step 2 says app passwords aren't available: 2-Step Verification must be on (not only a
security key), Advanced Protection must be off, and school/work accounts can block it.

## Part B — Build the app for free using GitHub (10 min, one time)

You don't need Android Studio. GitHub builds the app file (APK) for you.

1. Create a free account at https://github.com
2. Click **+** (top right) > **New repository**. Name it `MorningBrief`.
   **Public** is simplest (the code has no passwords in it). Click **Create repository**.
3. On the new repo page click **uploading an existing file**.
4. Unzip `MorningBrief.zip` on your computer, open the folder, select **everything inside it**
   (including the `.github` folder, `app` folder and the other files) and drag it into the page.
   Check the file list shows `.github`, `app`, `build.gradle.kts`, `settings.gradle.kts`, etc.
5. Click **Commit changes**.
6. Click the **Actions** tab. A run called "Build APK" starts. Wait about 5–10 minutes for a green tick.
7. Go back to the repo's main page. On the right, under **Releases**, click
   **Morning Brief (latest build)** and download **app-debug.apk**.

If the run shows a red X, click it, open the failed step, copy the red error text and send it to me —
I wrote this code without being able to compile it here, so a first-build fix is possible.

## Part C — Install on your phone

1. Open the same GitHub release page on your phone's browser and tap **app-debug.apk** to download
   (or send the file to your phone).
2. Open the downloaded file. Android asks you to allow installs from that app (browser/Files) — allow it.
3. Google Play Protect may warn "unknown developer". That's expected for apps not from the Play Store.
   Tap **More details > Install anyway**.

## Part D — First-time setup in the app

1. Allow **notifications** when asked.
2. Type your Gmail address and the app password, tap **Save login**.
3. Tap **Test email brief right now** — you should get a notification with your important emails.
   If it shows an error, the message tells you what's wrong.
4. Tap **Test alarm**. It rings; tap Dismiss; 15 seconds later the brief arrives.
5. Tap the two permission buttons:
   - **Allow full-screen alarm screen** (so the alarm screen appears over the lock screen)
   - **Stop battery saver from killing the alarm** — find Morning Brief and choose "Don't optimize"/"Unrestricted"
6. Pick your time, tap **Turn alarm ON**.

**Before relying on it for something important, do a real test:** set the alarm 2 minutes ahead, lock the phone, and see what happens. Some phone brands (Xiaomi, Oppo, Vivo, Realme, OnePlus, Samsung) have extra battery settings that can block alarms from apps not from the Play Store — see https://dontkillmyapp.com for your brand.

## Good to know

- Your Gmail address and app password are stored only inside the app on your phone (not encrypted).
  If you lose the phone, delete the app password at https://myaccount.google.com/apppasswords.
- The alarm uses your phone's default alarm sound and stops by itself after 10 minutes if you don't dismiss it
  (in that case no email brief is sent).
- "Important" means Gmail's own Important marker. It's decided by Google, so it can include some
  promotions and miss some things.
- Android may show a small alarm icon in the status bar while the daily alarm and the 10-minute timer are pending. That's normal.
- Each time you want a changed version: edit/replace files on GitHub; a new APK builds automatically.
  Install over the old one (your settings stay).

## Ideas to add later (just ask)

Snooze button · read the brief out loud · AI-written summary instead of a plain list ·
only certain senders/keywords · multiple alarms/weekdays only · Outlook support
