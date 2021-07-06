package com.mediatek.camera.feature.setting.demofb;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;

public class DemoFbRestriction {

	private static final String KEY_DEMO_FB = "key_demo_fb";
	private static final String HER10_KEY = "key_hdr10";
	public static final String ON = "on";
	public static final String OFF = "off";
	public static final String ON_AND_CHANGE_ROI = "changeRoi";

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
		sRelation.setHeaderKey(KEY_DEMO_FB);
		sRelation.setBodyKeys(HER10_KEY);
		sRelation.addRelation(
				new Relation.Builder(KEY_DEMO_FB, ON)
						.addBody(HER10_KEY,"off","off")
						.build());
		sRelation.addRelation(
				new Relation.Builder(KEY_DEMO_FB, ON_AND_CHANGE_ROI)
						.addBody(HER10_KEY,"off","off")
						.build());
	}

}
