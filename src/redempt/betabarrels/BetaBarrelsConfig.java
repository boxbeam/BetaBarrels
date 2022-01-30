package redempt.betabarrels;

import redempt.redlib.config.annotations.Comment;

public class BetaBarrelsConfig {

	@Comment("If enabled, barrels will drop themselves rather than the items they contain when broken")
	@Comment("Otherwise, barrels will drop all of the items in them, which may cause lag")
	public static boolean barrelDropSelf = true;

}
