/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.example.android.storageprovider


import android.content.Context
import android.os.Bundle
import android.provider.DocumentsContract
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import com.example.android.common.logger.Log
import com.example.android.storageprovider.GraphiteProvider.Companion.config
import org.blockstack.android.sdk.BlockstackSession

/**
 * Toggles the user's login status via a login menu option, and enables/disables the cloud storage
 * content provider.
 */
class StorageProviderFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    private var mSession: BlockstackSession? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mSession = BlockstackSession(context, config)
    }

    override fun onResume() {
        super.onResume()
        val auth = activity?.intent?.data?.getQueryParameter("authResponse")
        if (auth != null) {
            mSession!!.handlePendingSignIn(auth) {
                Log.d(TAG, it.value?.json.toString())
                activity!!.contentResolver.notifyChange(DocumentsContract.buildRootsUri(AUTHORITY), null, false)
            }
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu!!.findItem(R.id.sample_action)
        if (mSession != null) {
            item.setTitle(if (mSession!!.isUserSignedIn()) R.string.log_out else R.string.log_in)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.sample_action) {
            toggleLogin()
            item.setTitle(if (mSession!!.isUserSignedIn()) R.string.log_out else R.string.log_in)

            // BEGIN_INCLUDE(notify_change)
            // Notify the system that the status of our roots has changed.  This will trigger
            // a call to GraphiteProvider.queryRoots() and force a refresh of the system
            // picker UI.  It's important to call this or stale results may persist.
            activity!!.contentResolver.notifyChange(DocumentsContract.buildRootsUri(AUTHORITY), null, false)
            // END_INCLUDE(notify_change)
        }
        return true
    }

    /**
     * Dummy function to change the user's authorization status.
     */
    private fun toggleLogin() {
        // Replace this with your standard method of authentication to determine if your app
        // should make the user's documents available.
        if (mSession!!.isUserSignedIn()) {
            Log.i(TAG, getString(R.string.logged_in_info))
            mSession!!.signUserOut()
        } else {
            Log.i(TAG, getString(R.string.logged_out_info))
            mSession!!.redirectUserToSignIn { }
        }
    }

    companion object {

        private val TAG = "StorageProviderFragment"
        private val AUTHORITY = "com.example.android.storageprovider.documents"
    }

}


