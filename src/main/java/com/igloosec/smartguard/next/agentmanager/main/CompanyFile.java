package com.igloosec.smartguard.next.agentmanager.main;

import com.sk.snet.manipulates.EncryptUtil;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class CompanyFile {

    public static void main(String[] args) {

        System.out.println("CompanyFile Setting Start...");
        System.out.println(args[0]);
        System.out.println(args[1]);

        String output, companyKey;
        EncryptUtil.isCryptoCheck(true);
        try {
            companyKey = EncryptUtil.aes_encrypt(args[0]);

            output = "COMPANYKEY=" + companyKey + "\n";

            writeCompanyKey(output, args[1]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean writeCompanyKey(String companyKey, String path) {
        boolean ret;

        FileOutputStream fos =null;
        try {

            fos = new FileOutputStream(path, true);
            OutputStreamWriter writer = new OutputStreamWriter(fos);

            writer.append(companyKey);

            writer.flush();
            writer.close();
            fos.close();

            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            ret = false;
        }

        return ret;
    }
}
