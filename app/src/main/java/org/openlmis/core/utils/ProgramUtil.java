package org.openlmis.core.utils;
import org.openlmis.core.R;
public final class ProgramUtil {
    private ProgramUtil() {
    }

    public static int getThemeRes(String programCode) {
        switch (programCode) {
            case Constants.MMIA_PROGRAM_CODE:
                return R.style.AppTheme_AMBER;
            case Constants.VIA_PROGRAM_CODE:
                return R.style.AppTheme_PURPLE;
            case Constants.RAPID_TEST_CODE:
                return R.style.AppTheme_BlueGray;
            case Constants.AL_PROGRAM_CODE:
            default:
                return R.style.AppTheme_LIGHT_BLUE;
        }
    }

}
