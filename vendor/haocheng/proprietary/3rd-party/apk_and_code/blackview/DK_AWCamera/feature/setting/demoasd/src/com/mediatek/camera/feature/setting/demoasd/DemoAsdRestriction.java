package com.mediatek.camera.feature.setting.demoasd;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;

public class DemoAsdRestriction {

	private static final String KEY_DEMO_ASD = "key_demo_asd";
	private static final String HER10_KEY = "key_hdr10";
	public static final String ON = "on";
	public static final String OFF = "off";

	private static RelationGroup sRelation = new RelationGroup();

	/**
	 * Restriction witch are have setting ui.
	 *
	 * @return restriction list.
	 */
	static RelationGroup getRestriction() {
		return sRelation;
	}

	static {
		sRelation.setHeaderKey(KEY_DEMO_ASD);
		sRelation.setBodyKeys(HER10_KEY);
		sRelation.addRelation(
				new Relation.Builder(KEY_DEMO_ASD, ON)
						.addBody(HER10_KEY,"off","off")
						.build());
	}

}
