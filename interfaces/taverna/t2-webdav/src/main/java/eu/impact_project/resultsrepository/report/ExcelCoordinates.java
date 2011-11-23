/*
	
	Copyright 2011 The IMPACT Project
	
	@author Dennis Neumann

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
  		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

*/

package eu.impact_project.resultsrepository.report;

import java.util.Properties;

public class ExcelCoordinates {

	public CellCoordinates date = new CellCoordinates();
	public CellCoordinates overviewUpperRight = new CellCoordinates();
	public CellCoordinates processingTimesUpperLeft = new CellCoordinates();
	public CellCoordinates evaluationsUpperLeft = new CellCoordinates();
	public CellCoordinates inputUrlsUpperLeftHeader = new CellCoordinates();
	
	public class CellCoordinates {
		public int sheet;
		public int row;
		public int column;
	}
	
	public ExcelCoordinates(Properties props) {
		date.sheet = Integer.parseInt(props.getProperty("date.sheet")) - 1;
		date.row = Integer.parseInt(props.getProperty("date.row")) - 1;
		date.column = Integer.parseInt(props.getProperty("date.column")) - 1;
		
		overviewUpperRight.sheet = Integer.parseInt(props.getProperty("overviewUpperRight.sheet")) - 1;
		overviewUpperRight.row = Integer.parseInt(props.getProperty("overviewUpperRight.row")) - 1;
		overviewUpperRight.column = Integer.parseInt(props.getProperty("overviewUpperRight.column")) - 1;
		
		processingTimesUpperLeft.sheet = Integer.parseInt(props.getProperty("processingTimesUpperLeft.sheet")) - 1;
		processingTimesUpperLeft.row = Integer.parseInt(props.getProperty("processingTimesUpperLeft.row")) - 1;
		processingTimesUpperLeft.column = Integer.parseInt(props.getProperty("processingTimesUpperLeft.column")) - 1;

		evaluationsUpperLeft.sheet = Integer.parseInt(props.getProperty("evaluationsUpperLeft.sheet")) - 1;
		evaluationsUpperLeft.row = Integer.parseInt(props.getProperty("evaluationsUpperLeft.row")) - 1;
		evaluationsUpperLeft.column = Integer.parseInt(props.getProperty("evaluationsUpperLeft.column")) - 1;

		inputUrlsUpperLeftHeader.sheet = Integer.parseInt(props.getProperty("inputUrlsUpperLeftHeader.sheet")) - 1;
		inputUrlsUpperLeftHeader.row = Integer.parseInt(props.getProperty("inputUrlsUpperLeftHeader.row")) - 1;
		inputUrlsUpperLeftHeader.column = Integer.parseInt(props.getProperty("inputUrlsUpperLeftHeader.column")) - 1;
}
}
