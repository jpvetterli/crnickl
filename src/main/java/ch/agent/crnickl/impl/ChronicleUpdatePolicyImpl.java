/*
 *   Copyright 2012-2013 Hauser Olsson GmbH
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
 * 
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.t2.T2Exception;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeIndex;

/**
 * Default implementation of {@link ChronicleUpdatePolicy}.
 * 
 * @author Jean-Paul Vetterli
 */
public class ChronicleUpdatePolicyImpl implements ChronicleUpdatePolicy {
	
	private ChronicleUpdatePolicyExtension extension;

	/**
	 * Construct a {@link ChronicleUpdatePolicy}.
	 * 
	 * @param database a database
	 * @param extension a chronicle update policy extension
	 */
	public ChronicleUpdatePolicyImpl(DatabaseBackend database, ChronicleUpdatePolicyExtension extension) {
		this.extension = extension;
	}

	@Override
	public void willDelete(UpdatableChronicle entity) throws T2DBException {
		int entityCount = entity.getMembers().size();
		int seriesCount = entity.getSeries().size();
		if (entityCount > 0 || seriesCount > 0)
			throw T2DBMsg.exception(D.D40130, entity.getName(true), entityCount, seriesCount);
		if (extension != null)
			extension.willDelete(entity);
	}

	@Override
	public void willDelete(UpdatableSeries<?> series) throws T2DBException {
		try {
			Range range = series.getRange();
			if (!range.isEmpty())
				throw T2DBMsg.exception(D.D50130, series.getName(true), range.toString());
		} catch (T2Exception e) {
			throw T2DBMsg.exception(e, D.D50102, series.getName(true));
		}
		if (extension != null)
			extension.willDelete(series);
	}

	@Override
	public void willUpdate(UpdatableChronicle entity) throws T2DBException {
		if (extension != null)
			extension.willUpdate(entity);
	}

	@Override
	public void willUpdate(UpdatableSeries<?> series, Range range) throws T2DBException {
		if (extension != null)
			extension.willUpdate(series, range);
	}

	@Override
	public void willDelete(UpdatableSeries<?> series, TimeIndex index) throws T2DBException {
		if (extension != null)
			extension.willDelete(series, index);
	}

	@Override
	public boolean deleteChronicle(UpdatableChronicle entity) throws T2DBException {
		if (extension != null)
			return extension.deleteChronicle(entity);
		else
			return false;
	}

	@Override
	public boolean deleteSeries(UpdatableSeries<?> series) throws T2DBException {
		if (extension != null)
			return extension.deleteSeries(series);
		else
			return false;
	}

	@Override
	public boolean deleteValue(UpdatableSeries<?> series, TimeIndex index) throws T2DBException {
		if (extension != null)
			return extension.deleteValue(series, index);
		else
			return false;
	}

	@Override
	public boolean update(UpdatableSeries<?> series, Range range) throws T2DBException {
		if (extension != null)
			return extension.update(series, range);
		else
			return false;
	}
	
}
