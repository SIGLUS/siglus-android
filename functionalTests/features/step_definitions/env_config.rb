module EnvConfig

    def self.getConfig()

        dev_env=true

        if dev_env
            {
                username:"superuser",
                password:"password1",
                stockcard_name_MMIA:"Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg [08S42]"
                product_name_MMIA:"Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg"
            }
        else
            {
                username:"test_user",
                password:"testuser",
                stockcard_name_MMIA:"Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg [08S42]"
                product_name_MMIA:"Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg"
            }
        end
    end
end
