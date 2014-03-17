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
package org.fao.sola.clients.android.opentenure.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class Boundary {
	
	Boundary(){
		this.boundaryId = UUID.randomUUID().toString();
		vertices = new ArrayList<Vertex>();
	}

	@Override
	public String toString() {
		return "Boundary [boundaryId=" + boundaryId + ", vertices=" + Arrays.toString(vertices.toArray()) + "]";
	}
	public String getBoundaryId() {
		return boundaryId;
	}
	public void setBoundaryId(String boundaryId) {
		this.boundaryId = boundaryId;
	}
	public List<Vertex> getVertices() {
		return vertices;
	}
	
	public void setVertices(List<Vertex> vertices) {
		this.vertices = vertices;
	}

	public static int createBoundary(Boundary boundary) {
		int result = 0;
		for (int i = 0 ; i < boundary.getVertices().size() ; i++){
			result += Vertex.createVertex(boundary.getBoundaryId(), boundary.getVertices().get(i));
		}
		return result;
	}

	public int create() {
		int result = 0;
		for (int i = 0 ; i < vertices.size() ; i++){
			result += Vertex.createVertex(boundaryId, vertices.get(i));
		}
		return result;
	}

	public static int updateBoundary(Boundary boundary) {
		deleteBoundary(boundary);
		return createBoundary(boundary);
	}

	public int update() {
		delete();
		return create();
	}

	public int delete() {
		return OpenTenureApplication.getInstance().getDatabase().update("DELETE FROM BOUNDARY WHERE BOUNDARY_ID='"
				+ boundaryId
				+ "'");
	}

	public static int deleteBoundary(Boundary boundary) {
		return OpenTenureApplication.getInstance().getDatabase().update("DELETE FROM BOUNDARY WHERE BOUNDARY_ID='"
				+ boundary.getBoundaryId()
				+ "'"
				);
	}
	
	public static Boundary getBoundary(String boundaryId){
		Boundary boundary = new Boundary();
		boundary.setBoundaryId(boundaryId);
		boundary.setVertices(Vertex.getVertices(boundaryId));
		return boundary;
	}

	String boundaryId;
	List<Vertex> vertices;

}
