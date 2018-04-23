Github repository migration
------------


Since the 29th august 2016, Jawr repository location has changed from 

 https://github.com/ic3fox/... to  https://github.com/j-a-w-r/...

This migration should be transparent, however, to avoid confusion, we strongly recommend updating any existing local clones to point to the new repository URLs.

You will find below a migration guide to migrate from the old location to the new one.

-------------------------------------------------------------------

 
You can check if your repository is referencing the old location using :

	      git remote -v
	      
This should returns you something like :

	origin  https://github.com/ic3fox/jawr-main-repo.git (fetch)
	origin  https://github.com/ic3fox/jawr-main-repo.git (push)

To change the location, you'll have to use the following command

	git remote set-url origin git@github.com:j-a-w-r/jawr-main-repo.git

If you launch the following command again

	      git remote -v

You'll get :

	origin  https://github.com/j-a-w-r/jawr-main-repo.git (fetch)
	origin  https://github.com/j-a-w-r/jawr-main-repo.git (push)

If you have other repository which points to the old location, you can use the same command to change the location.