/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.fao.sola.clients.android.opentenure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;

public class SubmittedClaimsFragment extends SeparatedListFragment implements
		OnTouchListener {
	private AlphabetListAdapter adapter = new AlphabetListAdapter();
	private GestureDetector mGestureDetector;

	public SubmittedClaimsFragment() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.submitted_claims, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		reset();
		rootView = inflater.inflate(R.layout.submitted_claims_list, container,
				false);
		setHasOptionsMenu(true);

		mGestureDetector = new GestureDetector(rootView.getContext(),
				new SideIndexGestureListener());

		List<String> submittedClaims = populateList();
		Collections.sort(submittedClaims);

		List<AlphabetListAdapter.Row> rows = getRows(submittedClaims);

		adapter.setRows(rows);
		setListAdapter(adapter);

		updateList();
		return rootView;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		} else {
			return false;
		}
	}

	protected List<String> populateList() {
		List<String> claims = new ArrayList<String>();
		claims.add("Awwww\nClaim 1: Submitted");
		claims.add("Axxxx\nClaim 1: Submitted");
		claims.add("Ayyyy\nClaim 1: Submitted");
		claims.add("Azzzz\nClaim 1: Submitted");
		claims.add("Bwwww\nClaim 1: Submitted");
		claims.add("Bxxxx\nClaim 1: Submitted");
		claims.add("Byyyy\nClaim 1: Submitted");
		claims.add("Bzzzz\nClaim 1: Submitted");
		claims.add("Lwwww\nClaim 1: Submitted");
		claims.add("Lxxxx\nClaim 1: Submitted");
		claims.add("Lyyyy\nClaim 1: Submitted");
		claims.add("Lzzzz\nClaim 1: Submitted");
		claims.add("Fwwww\nClaim 1: Submitted");
		claims.add("Fxxxx\nClaim 1: Submitted");
		claims.add("Fyyyy\nClaim 1: Submitted");
		claims.add("Fzzzz\nClaim 1: Submitted");
		claims.add("Gwwww\nClaim 1: Submitted");
		claims.add("Gxxxx\nClaim 1: Submitted");
		claims.add("Gyyyy\nClaim 1: Submitted");
		claims.add("Gzzzz\nClaim 1: Submitted");
		claims.add("0wwww\nClaim 1: Submitted");
		claims.add("0xxxx\nClaim 1: Submitted");
		claims.add("0yyyy\nClaim 1: Submitted");
		claims.add("0zzzz\nClaim 1: Submitted");
		claims.add("Hwwww\nClaim 1: Submitted");
		claims.add("Hxxxx\nClaim 1: Submitted");
		claims.add("Hyyyy\nClaim 1: Submitted");
		claims.add("Hzzzz\nClaim 1: Submitted");
		claims.add("Nwwww\nClaim 1: Submitted");
		claims.add("Nxxxx\nClaim 1: Submitted");
		claims.add("Nyyyy\nClaim 1: Submitted");
		claims.add("Nzzzz\nClaim 1: Submitted");
		claims.add("Swwww\nClaim 1: Submitted");
		claims.add("Sxxxx\nClaim 1: Submitted");
		claims.add("Syyyy\nClaim 1: Submitted");
		claims.add("Szzzz\nClaim 1: Submitted");
		claims.add("Jwwww\nClaim 1: Submitted");
		claims.add("Jxxxx\nClaim 1: Submitted");
		claims.add("Jyyyy\nClaim 1: Submitted");
		claims.add("Jzzzz\nClaim 1: Submitted");
		claims.add("Qwwww\nClaim 1: Submitted");
		claims.add("Qxxxx\nClaim 1: Submitted");
		claims.add("Qyyyy\nClaim 1: Submitted");
		claims.add("Qzzzz\nClaim 1: Submitted");
		claims.add("Kwwww\nClaim 1: Submitted");
		claims.add("Kxxxx\nClaim 1: Submitted");
		claims.add("Kyyyy\nClaim 1: Submitted");
		claims.add("Kzzzz\nClaim 1: Submitted");
		return claims;
	}
}