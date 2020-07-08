package xjunz.tool.wechat.impl.model.account;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import org.apaches.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;

import xjunz.tool.wechat.impl.Environment;
import xjunz.tool.wechat.impl.repo.AvatarRepository;
import xjunz.tool.wechat.util.ShellUtils;


/**
 * 微信账号（个人用户、群聊、公众号）的抽象类
 */
public abstract class Account implements Serializable {
    /**
     * 微信的昵称
     */
    public String nickname;
    /**
     * 微信号，是微信账号或有的唯一标识
     */
    public String alias;
    private String originalAvatarPath;
    private String backupAvatarPath;
    /**
     * 微信ID，通常的形式是"wxid_xxxxx"，是微信账号必有的唯一标识
     */
    public String id;
    private String pathIdentifier;
    private String ownerDirPath;
    /**
     * 当前用户的UIN，是当前用户的唯一标识
     */
    private String ownerUin;
    private boolean hasLocalAvatar = true;

    String getPathIdentifier() {
        return pathIdentifier;
    }

    String getOwnerDirPath() {
        return ownerDirPath;
    }


    /**
     * @return 当前账号是否为公众号
     */
    protected boolean isGZH() {
        return id.startsWith("gh_");
    }

    /**
     * 判断当前账号是否为个人用户号，此方法是充分不必要判断，
     * 充要判断请使用{@link Account#isUser()}
     *
     * @return 是否为个人用户号
     */
    protected boolean isUserUnnecessary() {
        return id.startsWith("wxid_");
    }

    /**
     * @return 当前账号是否为个人用户号
     */
    public boolean isUser() {
        return !isGZH() && !isGroup();
    }

    /**
     * @return 当前账号是否为群聊号
     */
    protected boolean isGroup() {
        return id.endsWith("@chatroom");
    }

    Account(String uin) {
        this.ownerUin = uin;
        this.pathIdentifier = DigestUtils.md5Hex("mm" + ownerUin);
        this.ownerDirPath = Environment.getInstance().getWechatMicroMsgPath() + File.separator + pathIdentifier;
    }


    public void endowIdentity(String id) {
        this.id = id;
        String idMd5 = DigestUtils.md5Hex(id);
        this.backupAvatarPath = Environment.getInstance().getAvatarBackupPath() + File.separator + idMd5;
        this.originalAvatarPath = this.ownerDirPath + File.separator + "avatar" + File.separator
                + idMd5.substring(0, 2) + File.separator
                + idMd5.substring(2, 4) + File.separator
                + "user_" + idMd5 + ".png";
    }

    protected boolean empty(String str) {
        return str == null || str.length() == 0;
    }


    public String getName() {
        return empty(nickname) ? (empty(alias) ? (empty(id) ? "<unknown>" : id) : alias) : nickname;
    }

    public String getOwnerUin() {
        return ownerUin;
    }

    @Nullable
    private Bitmap decodeAvatar() {
        File backup = new File(backupAvatarPath);
        if (!backup.exists()) {
            ShellUtils.cpNoError(originalAvatarPath, backupAvatarPath);
        }
        Bitmap bitmap = BitmapFactory.decodeFile(backupAvatarPath);
        this.hasLocalAvatar = bitmap != null;
        return bitmap;
    }


    @Nullable
    public Bitmap getAvatar() {
        if (hasLocalAvatar) {
            Bitmap bitmap = AvatarRepository.getInstance().getAvatarOf(id);
            if (bitmap == null) {
                bitmap = decodeAvatar();
                if (bitmap != null) {
                    AvatarRepository.getInstance().putAvatarOf(id, bitmap);
                }
            }
            return bitmap;
        } else {
            return null;
        }
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Account) {
            return ((Account) obj).id.equals(id);
        }
        return super.equals(obj);
    }

    @NotNull
    @Override
    public String toString() {
        return "Account{" +
                "nickname='" + nickname + '\'' +
                ", alias='" + alias + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
